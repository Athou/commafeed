package com.commafeed.backend;

import java.io.IOException;
import java.io.InputStream;
import java.net.IDN;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.jboss.resteasy.reactive.common.headers.CacheControlDelegate;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.HttpClientCache;
import com.commafeed.CommaFeedVersion;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.CacheControl;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.apache5.util.Apache5SslUtils;

/**
 * Smart HTTP getter: handles gzip, ssl, last modified and etag headers
 */
@Singleton
@Slf4j
public class HttpGetter {

	private final CommaFeedConfiguration config;
	private final CloseableHttpClient client;
	private final Cache<HttpRequest, HttpResponse> cache;

	public HttpGetter(CommaFeedConfiguration config, CommaFeedVersion version, MetricRegistry metrics) {
		this.config = config;

		PoolingHttpClientConnectionManager connectionManager = newConnectionManager(config);
		String userAgent = config.httpClient()
				.userAgent()
				.orElseGet(() -> String.format("CommaFeed/%s (https://github.com/Athou/commafeed)", version.getVersion()));

		this.client = newClient(connectionManager, userAgent, config.httpClient().idleConnectionsEvictionInterval());
		this.cache = newCache(config);

		metrics.registerGauge(MetricRegistry.name(getClass(), "pool", "max"), () -> connectionManager.getTotalStats().getMax());
		metrics.registerGauge(MetricRegistry.name(getClass(), "pool", "size"),
				() -> connectionManager.getTotalStats().getAvailable() + connectionManager.getTotalStats().getLeased());
		metrics.registerGauge(MetricRegistry.name(getClass(), "pool", "leased"), () -> connectionManager.getTotalStats().getLeased());
		metrics.registerGauge(MetricRegistry.name(getClass(), "pool", "pending"), () -> connectionManager.getTotalStats().getPending());
		metrics.registerGauge(MetricRegistry.name(getClass(), "cache", "size"), () -> cache == null ? 0 : cache.size());
		metrics.registerGauge(MetricRegistry.name(getClass(), "cache", "memoryUsage"),
				() -> cache == null ? 0 : cache.asMap().values().stream().mapToInt(e -> e.content != null ? e.content.length : 0).sum());
	}

	public HttpResult get(String url) throws IOException, NotModifiedException {
		return get(HttpRequest.builder(url).build());
	}

	public HttpResult get(HttpRequest request) throws IOException, NotModifiedException {
		final HttpResponse response;
		if (cache == null) {
			response = invoke(request);
		} else {
			try {
				response = cache.get(request, () -> invoke(request));
			} catch (ExecutionException e) {
				if (e.getCause() instanceof IOException ioe) {
					throw ioe;
				} else {
					throw new RuntimeException(e);
				}
			}
		}

		int code = response.getCode();
		if (code == HttpStatus.SC_NOT_MODIFIED) {
			throw new NotModifiedException("'304 - not modified' http code received");
		} else if (code >= 300) {
			throw new HttpResponseException(code, "Server returned HTTP error code " + code);
		}

		String lastModifiedHeader = response.getLastModifiedHeader();
		if (lastModifiedHeader != null && lastModifiedHeader.equals(request.getLastModified())) {
			throw new NotModifiedException("lastModifiedHeader is the same");
		}

		String eTagHeader = response.getETagHeader();
		if (eTagHeader != null && eTagHeader.equals(request.getETag())) {
			throw new NotModifiedException("eTagHeader is the same");
		}

		Duration validFor = Optional.ofNullable(response.getCacheControl())
				.filter(cc -> cc.getMaxAge() >= 0)
				.map(cc -> Duration.ofSeconds(cc.getMaxAge()))
				.orElse(Duration.ZERO);

		return new HttpResult(response.getContent(), response.getContentType(), lastModifiedHeader, eTagHeader,
				response.getUrlAfterRedirect(), validFor);
	}

	private HttpResponse invoke(HttpRequest request) throws IOException {
		log.debug("fetching {}", request.getUrl());

		HttpClientContext context = HttpClientContext.create();
		context.setRequestConfig(RequestConfig.custom()
				.setResponseTimeout(Timeout.of(config.httpClient().responseTimeout()))
				// causes issues with some feeds
				// see https://github.com/Athou/commafeed/issues/1572
				// and https://issues.apache.org/jira/browse/HTTPCLIENT-2344
				.setProtocolUpgradeEnabled(false)
				.build());

		return client.execute(request.toClassicHttpRequest(), context, resp -> {
			byte[] content = resp.getEntity() == null ? null
					: toByteArray(resp.getEntity(), config.httpClient().maxResponseSize().asLongValue());
			int code = resp.getCode();
			String lastModifiedHeader = Optional.ofNullable(resp.getFirstHeader(HttpHeaders.LAST_MODIFIED))
					.map(NameValuePair::getValue)
					.map(StringUtils::trimToNull)
					.orElse(null);
			String eTagHeader = Optional.ofNullable(resp.getFirstHeader(HttpHeaders.ETAG))
					.map(NameValuePair::getValue)
					.map(StringUtils::trimToNull)
					.orElse(null);

			CacheControl cacheControl = Optional.ofNullable(resp.getFirstHeader(HttpHeaders.CACHE_CONTROL))
					.map(NameValuePair::getValue)
					.map(StringUtils::trimToNull)
					.map(HttpGetter::toCacheControl)
					.orElse(null);

			String contentType = Optional.ofNullable(resp.getEntity()).map(HttpEntity::getContentType).orElse(null);
			String urlAfterRedirect = Optional.ofNullable(context.getRedirectLocations())
					.map(RedirectLocations::getAll)
					.map(l -> Iterables.getLast(l, null))
					.map(URI::toString)
					.orElse(request.getUrl());

			return new HttpResponse(code, lastModifiedHeader, eTagHeader, cacheControl, content, contentType, urlAfterRedirect);
		});
	}

	private static CacheControl toCacheControl(String headerValue) {
		try {
			return CacheControlDelegate.INSTANCE.fromString(headerValue);
		} catch (Exception e) {
			log.debug("Invalid Cache-Control header: {}", headerValue);
			return null;
		}
	}

	private static byte[] toByteArray(HttpEntity entity, long maxBytes) throws IOException {
		if (entity.getContentLength() > maxBytes) {
			throw new IOException(
					"Response size (%s bytes) exceeds the maximum allowed size (%s bytes)".formatted(entity.getContentLength(), maxBytes));
		}

		try (InputStream input = entity.getContent()) {
			if (input == null) {
				return null;
			}

			byte[] bytes = ByteStreams.limit(input, maxBytes).readAllBytes();
			if (bytes.length == maxBytes) {
				throw new IOException("Response size exceeds the maximum allowed size (%s bytes)".formatted(maxBytes));
			}
			return bytes;
		}
	}

	private static PoolingHttpClientConnectionManager newConnectionManager(CommaFeedConfiguration config) {
		SSLFactory sslFactory = SSLFactory.builder().withUnsafeTrustMaterial().withUnsafeHostnameVerifier().build();

		int poolSize = config.feedRefresh().httpThreads();
		return PoolingHttpClientConnectionManagerBuilder.create()
				.setTlsSocketStrategy(Apache5SslUtils.toTlsSocketStrategy(sslFactory))
				.setDefaultConnectionConfig(ConnectionConfig.custom()
						.setConnectTimeout(Timeout.of(config.httpClient().connectTimeout()))
						.setSocketTimeout(Timeout.of(config.httpClient().socketTimeout()))
						.setTimeToLive(Timeout.of(config.httpClient().connectionTimeToLive()))
						.build())
				.setDefaultTlsConfig(TlsConfig.custom().setHandshakeTimeout(Timeout.of(config.httpClient().sslHandshakeTimeout())).build())
				.setMaxConnPerRoute(poolSize)
				.setMaxConnTotal(poolSize)
				.setDnsResolver(new InternationalizedDomainNameToAsciiDnsResolver(SystemDefaultDnsResolver.INSTANCE))
				.build();

	}

	private static CloseableHttpClient newClient(HttpClientConnectionManager connectionManager, String userAgent,
			Duration idleConnectionsEvictionInterval) {
		List<Header> headers = new ArrayList<>();
		headers.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "en"));
		headers.add(new BasicHeader(HttpHeaders.PRAGMA, "No-cache"));
		headers.add(new BasicHeader(HttpHeaders.CACHE_CONTROL, "no-cache"));

		return HttpClientBuilder.create()
				.useSystemProperties()
				.disableAutomaticRetries()
				.disableCookieManagement()
				.setUserAgent(userAgent)
				.setDefaultHeaders(headers)
				.setConnectionManager(connectionManager)
				.evictExpiredConnections()
				.evictIdleConnections(TimeValue.of(idleConnectionsEvictionInterval))
				.build();
	}

	private static Cache<HttpRequest, HttpResponse> newCache(CommaFeedConfiguration config) {
		HttpClientCache cacheConfig = config.httpClient().cache();
		if (!cacheConfig.enabled()) {
			return null;
		}

		return CacheBuilder.newBuilder()
				.weigher((HttpRequest key, HttpResponse value) -> value.getContent() != null ? value.getContent().length : 0)
				.maximumWeight(cacheConfig.maximumMemorySize().asLongValue())
				.expireAfterWrite(cacheConfig.expiration())
				.build();
	}

	private record InternationalizedDomainNameToAsciiDnsResolver(DnsResolver delegate) implements DnsResolver {
		@Override
		public InetAddress[] resolve(String host) throws UnknownHostException {
			return delegate.resolve(IDN.toASCII(host));
		}

		@Override
		public String resolveCanonicalHostname(String host) throws UnknownHostException {
			return delegate.resolveCanonicalHostname(IDN.toASCII(host));
		}
	}

	@Getter
	public static class NotModifiedException extends Exception {
		private static final long serialVersionUID = 1L;

		/**
		 * if the value of this header changed, this is its new value
		 */
		private final String newLastModifiedHeader;

		/**
		 * if the value of this header changed, this is its new value
		 */
		private final String newEtagHeader;

		public NotModifiedException(String message) {
			this(message, null, null);
		}

		public NotModifiedException(String message, String newLastModifiedHeader, String newEtagHeader) {
			super(message);
			this.newLastModifiedHeader = newLastModifiedHeader;
			this.newEtagHeader = newEtagHeader;
		}
	}

	@Getter
	public static class HttpResponseException extends IOException {
		private static final long serialVersionUID = 1L;

		private final int code;

		public HttpResponseException(int code, String message) {
			super(message);
			this.code = code;
		}
	}

	@Builder(builderMethodName = "")
	@EqualsAndHashCode
	@Getter
	public static class HttpRequest {
		private String url;
		private String lastModified;
		private String eTag;

		public static HttpRequestBuilder builder(String url) {
			return new HttpRequestBuilder().url(url);
		}

		public ClassicHttpRequest toClassicHttpRequest() {
			ClassicHttpRequest req = ClassicRequestBuilder.get(url).build();
			if (lastModified != null) {
				req.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
			}
			if (eTag != null) {
				req.addHeader(HttpHeaders.IF_NONE_MATCH, eTag);
			}
			return req;
		}
	}

	@Value
	private static class HttpResponse {
		int code;
		String lastModifiedHeader;
		String eTagHeader;
		CacheControl cacheControl;
		byte[] content;
		String contentType;
		String urlAfterRedirect;
	}

	@Value
	public static class HttpResult {
		byte[] content;
		String contentType;
		String lastModifiedSince;
		String eTag;
		String urlAfterRedirect;
		Duration validFor;
	}

}
