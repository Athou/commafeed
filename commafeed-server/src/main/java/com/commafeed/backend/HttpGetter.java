package com.commafeed.backend;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
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

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedVersion;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

import io.quarkus.runtime.configuration.MemorySize;
import jakarta.inject.Singleton;
import lombok.Getter;
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

	private final CloseableHttpClient client;
	private final MemorySize maxResponseSize;

	public HttpGetter(CommaFeedConfiguration config, CommaFeedVersion version, MetricRegistry metrics) {
		PoolingHttpClientConnectionManager connectionManager = newConnectionManager(config.feedRefresh().httpThreads());
		String userAgent = config.feedRefresh()
				.userAgent()
				.orElseGet(() -> String.format("CommaFeed/%s (https://github.com/Athou/commafeed)", version.getVersion()));
		this.client = newClient(connectionManager, userAgent);
		this.maxResponseSize = config.feedRefresh().maxResponseSize();

		metrics.registerGauge(MetricRegistry.name(getClass(), "pool", "max"), () -> connectionManager.getTotalStats().getMax());
		metrics.registerGauge(MetricRegistry.name(getClass(), "pool", "size"),
				() -> connectionManager.getTotalStats().getAvailable() + connectionManager.getTotalStats().getLeased());
		metrics.registerGauge(MetricRegistry.name(getClass(), "pool", "leased"), () -> connectionManager.getTotalStats().getLeased());
		metrics.registerGauge(MetricRegistry.name(getClass(), "pool", "pending"), () -> connectionManager.getTotalStats().getPending());
	}

	public HttpResult getBinary(String url, int timeout) throws IOException, NotModifiedException {
		return getBinary(url, null, null, timeout);
	}

	/**
	 * @param url
	 *            the url to retrive
	 * @param lastModified
	 *            header we got last time we queried that url, or null
	 * @param eTag
	 *            header we got last time we queried that url, or null
	 * @throws NotModifiedException
	 *             if the url hasn't changed since we asked for it last time
	 */
	public HttpResult getBinary(String url, String lastModified, String eTag, int timeout) throws IOException, NotModifiedException {
		log.debug("fetching {}", url);

		long start = System.currentTimeMillis();
		ClassicHttpRequest request = ClassicRequestBuilder.get(url).build();
		if (lastModified != null) {
			request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
		}
		if (eTag != null) {
			request.addHeader(HttpHeaders.IF_NONE_MATCH, eTag);
		}

		HttpClientContext context = HttpClientContext.create();
		context.setRequestConfig(RequestConfig.custom().setResponseTimeout(timeout, TimeUnit.MILLISECONDS).build());

		HttpResponse response = client.execute(request, context, resp -> {
			byte[] content = resp.getEntity() == null ? null : toByteArray(resp.getEntity(), maxResponseSize.asLongValue());
			int code = resp.getCode();
			String lastModifiedHeader = Optional.ofNullable(resp.getFirstHeader(HttpHeaders.LAST_MODIFIED))
					.map(NameValuePair::getValue)
					.map(StringUtils::trimToNull)
					.orElse(null);
			String eTagHeader = Optional.ofNullable(resp.getFirstHeader(HttpHeaders.ETAG))
					.map(NameValuePair::getValue)
					.map(StringUtils::trimToNull)
					.orElse(null);

			String contentType = Optional.ofNullable(resp.getEntity()).map(HttpEntity::getContentType).orElse(null);
			String urlAfterRedirect = Optional.ofNullable(context.getRedirectLocations())
					.map(RedirectLocations::getAll)
					.map(l -> Iterables.getLast(l, null))
					.map(URI::toString)
					.orElse(url);

			return new HttpResponse(code, lastModifiedHeader, eTagHeader, content, contentType, urlAfterRedirect);
		});

		int code = response.getCode();
		if (code == HttpStatus.SC_NOT_MODIFIED) {
			throw new NotModifiedException("'304 - not modified' http code received");
		} else if (code >= 300) {
			throw new HttpResponseException(code, "Server returned HTTP error code " + code);
		}

		String lastModifiedHeader = response.getLastModifiedHeader();
		if (lastModifiedHeader != null && lastModifiedHeader.equals(lastModified)) {
			throw new NotModifiedException("lastModifiedHeader is the same");
		}

		String eTagHeader = response.getETagHeader();
		if (eTagHeader != null && eTagHeader.equals(eTag)) {
			throw new NotModifiedException("eTagHeader is the same");
		}

		long duration = System.currentTimeMillis() - start;
		return new HttpResult(response.getContent(), response.getContentType(), lastModifiedHeader, eTagHeader, duration,
				response.getUrlAfterRedirect());
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

	private static PoolingHttpClientConnectionManager newConnectionManager(int poolSize) {
		SSLFactory sslFactory = SSLFactory.builder().withUnsafeTrustMaterial().withUnsafeHostnameVerifier().build();

		return PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(Apache5SslUtils.toSocketFactory(sslFactory))
				.setDefaultConnectionConfig(
						ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(5)).setTimeToLive(TimeValue.ofSeconds(30)).build())
				.setDefaultTlsConfig(TlsConfig.custom().setHandshakeTimeout(Timeout.ofSeconds(5)).build())
				.setMaxConnPerRoute(poolSize)
				.setMaxConnTotal(poolSize)
				.build();

	}

	private static CloseableHttpClient newClient(HttpClientConnectionManager connectionManager, String userAgent) {
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
				.evictIdleConnections(TimeValue.ofMinutes(1))
				.build();
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

	@Getter
	@RequiredArgsConstructor
	private static class HttpResponse {
		private final int code;
		private final String lastModifiedHeader;
		private final String eTagHeader;
		private final byte[] content;
		private final String contentType;
		private final String urlAfterRedirect;
	}

	@Getter
	@RequiredArgsConstructor
	public static class HttpResult {
		private final byte[] content;
		private final String contentType;
		private final String lastModifiedSince;
		private final String eTag;
		private final long duration;
		private final String urlAfterRedirect;
	}

}
