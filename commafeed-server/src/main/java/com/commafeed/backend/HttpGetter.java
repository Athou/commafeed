package com.commafeed.backend;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.CacheControl;

import org.apache.commons.lang3.ArrayUtils;
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
import org.apache.hc.client5.http.utils.DateUtils;
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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Lombok;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.apache5.util.Apache5SslUtils;

/**
 * Smart HTTP getter: handles gzip, ssl, last modified and etag headers
 */
@Singleton
@Slf4j
public class HttpGetter {
	private static final DnsResolver DNS_RESOLVER = SystemDefaultDnsResolver.INSTANCE;

	private final CommaFeedConfiguration config;
	private final InstantSource instantSource;
	private final CloseableHttpClient client;
	private final Cache<HttpRequest, HttpResponse> cache;

	public HttpGetter(CommaFeedConfiguration config, InstantSource instantSource, CommaFeedVersion version, MetricRegistry metrics) {
		this.config = config;
		this.instantSource = instantSource;

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
				() -> cache == null ? 0 : cache.asMap().values().stream().mapToInt(e -> ArrayUtils.getLength(e.content)).sum());
	}

	public HttpResult get(String url)
			throws IOException, NotModifiedException, TooManyRequestsException, SchemeNotAllowedException, HostNotAllowedException {
		return get(HttpRequest.builder(url).build());
	}

	public HttpResult get(HttpRequest request)
			throws IOException, NotModifiedException, TooManyRequestsException, SchemeNotAllowedException, HostNotAllowedException {
		URI uri = URI.create(request.getUrl());
		ensureHttpScheme(uri.getScheme());

		if (config.httpClient().blockLocalAddresses()) {
			ensurePublicAddress(uri.getHost());
		}

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
					throw Lombok.sneakyThrow(e);
				}
			}
		}

		int code = response.code();
		if (code == HttpStatus.SC_TOO_MANY_REQUESTS || code == HttpStatus.SC_SERVICE_UNAVAILABLE && response.retryAfter() != null) {
			throw new TooManyRequestsException(response.retryAfter());
		}

		if (code == HttpStatus.SC_NOT_MODIFIED) {
			throw new NotModifiedException("'304 - not modified' http code received");
		}

		if (code >= 300) {
			throw new HttpResponseException(code, "Server returned HTTP error code " + code);
		}

		String lastModifiedHeader = response.lastModifiedHeader();
		String eTagHeader = response.eTagHeader();

		Duration validFor = Optional.ofNullable(response.cacheControl())
				.filter(cc -> cc.getMaxAge() >= 0)
				.map(cc -> Duration.ofSeconds(cc.getMaxAge()))
				.orElse(Duration.ZERO);

		return new HttpResult(response.content(), response.contentType(), lastModifiedHeader, eTagHeader, response.urlAfterRedirect(),
				validFor);
	}

	private void ensureHttpScheme(String scheme) throws SchemeNotAllowedException {
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			throw new SchemeNotAllowedException(scheme);
		}
	}

	private void ensurePublicAddress(String host) throws HostNotAllowedException, UnknownHostException {
		if (host == null) {
			throw new HostNotAllowedException(null);
		}

		InetAddress[] addresses = DNS_RESOLVER.resolve(host);
		if (Stream.of(addresses).anyMatch(this::isPrivateAddress)) {
			throw new HostNotAllowedException(host);
		}
	}

	private boolean isPrivateAddress(InetAddress address) {
		return address.isSiteLocalAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isLoopbackAddress()
				|| address.isMulticastAddress();
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

			Instant retryAfter = Optional.ofNullable(resp.getFirstHeader(HttpHeaders.RETRY_AFTER))
					.map(NameValuePair::getValue)
					.map(StringUtils::trimToNull)
					.map(this::toInstant)
					.orElse(null);

			String contentType = Optional.ofNullable(resp.getEntity()).map(HttpEntity::getContentType).orElse(null);
			String urlAfterRedirect = Optional.ofNullable(context.getRedirectLocations())
					.map(RedirectLocations::getAll)
					.map(l -> Iterables.getLast(l, null))
					.map(URI::toString)
					.orElse(request.getUrl());

			return new HttpResponse(code, lastModifiedHeader, eTagHeader, cacheControl, retryAfter, content, contentType, urlAfterRedirect);
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

	private Instant toInstant(String headerValue) {
		if (headerValue == null) {
			return null;
		}

		if (StringUtils.isNumeric(headerValue)) {
			return instantSource.instant().plusSeconds(Long.parseLong(headerValue));
		}

		return DateUtils.parseStandardDate(headerValue);
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

			byte[] bytes = ByteStreams.limit(input, maxBytes + 1).readAllBytes();
			if (bytes.length > maxBytes) {
				throw new IOException("Response size exceeds the maximum allowed size (%s bytes)".formatted(maxBytes));
			}
			return bytes;
		}
	}

	private PoolingHttpClientConnectionManager newConnectionManager(CommaFeedConfiguration config) {
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
				.setDnsResolver(DNS_RESOLVER)
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
				.weigher((HttpRequest key, HttpResponse value) -> value.content() != null ? value.content().length : 0)
				.maximumWeight(cacheConfig.maximumMemorySize().asLongValue())
				.expireAfterWrite(cacheConfig.expiration())
				.build();
	}

	public static class SchemeNotAllowedException extends Exception {
		private static final long serialVersionUID = 1L;

		public SchemeNotAllowedException(String scheme) {
			super("Scheme not allowed: " + scheme);
		}
	}

	public static class HostNotAllowedException extends Exception {
		private static final long serialVersionUID = 1L;

		public HostNotAllowedException(String host) {
			super("Host not allowed: " + host);
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

	@RequiredArgsConstructor
	@Getter
	public static class TooManyRequestsException extends Exception {
		private static final long serialVersionUID = 1L;

		private final Instant retryAfter;
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

	private record HttpResponse(int code, String lastModifiedHeader, String eTagHeader, CacheControl cacheControl, Instant retryAfter,
			byte[] content, String contentType, String urlAfterRedirect) {
	}

	public record HttpResult(byte[] content, String contentType, String lastModifiedSince, String eTag, String urlAfterRedirect,
			Duration validFor) {
	}

}
