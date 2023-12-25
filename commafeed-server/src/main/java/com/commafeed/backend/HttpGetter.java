package com.commafeed.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;

import com.commafeed.CommaFeedConfiguration;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;

import io.dropwizard.lifecycle.Managed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.altindag.ssl.SSLFactory;

/**
 * Smart HTTP getter: handles gzip, ssl, last modified and etag headers
 *
 */
@Singleton
public class HttpGetter implements Managed {

	private final CloseableHttpClient client;

	@Inject
	public HttpGetter(CommaFeedConfiguration config) {
		String userAgent = Optional.ofNullable(config.getApplicationSettings().getUserAgent())
				.orElseGet(() -> String.format("CommaFeed/%s (https://github.com/Athou/commafeed)", config.getVersion()));
		this.client = newClient(userAgent, config.getApplicationSettings().getBackgroundThreads());
	}

	public HttpResult getBinary(String url, int timeout) throws IOException, NotModifiedException, InterruptedException {
		return getBinary(url, null, null, timeout);
	}

	/**
	 *
	 * @param url
	 *            the url to retrive
	 * @param lastModified
	 *            header we got last time we queried that url, or null
	 * @param eTag
	 *            header we got last time we queried that url, or null
	 * @throws NotModifiedException
	 *             if the url hasn't changed since we asked for it last time
	 */
	public HttpResult getBinary(String url, String lastModified, String eTag, int timeout)
			throws IOException, NotModifiedException, InterruptedException {
		long start = System.currentTimeMillis();

		HttpGet request = new HttpGet(url);
		if (lastModified != null) {
			request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
		}
		if (eTag != null) {
			request.addHeader(HttpHeaders.IF_NONE_MATCH, eTag);
		}

		HttpClientContext context = HttpClientContext.create();
		context.setRequestConfig(
				RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build());

		try (CloseableHttpResponse response = client.execute(request, context)) {
			int code = response.getStatusLine().getStatusCode();
			if (code == HttpStatus.NOT_MODIFIED_304) {
				throw new NotModifiedException("'304 - not modified' http code received");
			} else if (code >= 300) {
				throw new HttpResponseException(code, "Server returned HTTP error code " + code);
			}

			String lastModifiedHeader = Optional.ofNullable(response.getFirstHeader(HttpHeaders.LAST_MODIFIED))
					.map(NameValuePair::getValue)
					.map(StringUtils::trimToNull)
					.orElse(null);
			if (lastModifiedHeader != null && lastModifiedHeader.equals(lastModified)) {
				throw new NotModifiedException("lastModifiedHeader is the same");
			}

			String eTagHeader = Optional.ofNullable(response.getFirstHeader(HttpHeaders.ETAG))
					.map(NameValuePair::getValue)
					.map(StringUtils::trimToNull)
					.orElse(null);
			if (eTagHeader != null && eTagHeader.equals(eTag)) {
				throw new NotModifiedException("eTagHeader is the same");
			}

			HttpEntity entity = response.getEntity();
			byte[] content = entity == null ? null : EntityUtils.toByteArray(entity);
			String contentType = Optional.ofNullable(entity).map(HttpEntity::getContentType).map(Header::getValue).orElse(null);
			String urlAfterRedirect = CollectionUtils.isEmpty(context.getRedirectLocations()) ? url
					: Iterables.getLast(context.getRedirectLocations()).toString();

			long duration = System.currentTimeMillis() - start;
			return new HttpResult(content, contentType, lastModifiedHeader, eTagHeader, duration, urlAfterRedirect);
		}
	}

	private CloseableHttpClient newClient(String userAgent, int poolSize) {
		SSLFactory sslFactory = SSLFactory.builder().withUnsafeTrustMaterial().withUnsafeHostnameVerifier().build();

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
				.setSSLContext(sslFactory.getSslContext())
				.setSSLHostnameVerifier(sslFactory.getHostnameVerifier())
				.setMaxConnTotal(poolSize)
				.setMaxConnPerRoute(poolSize)
				.build();
	}

	@Override
	public void stop() throws Exception {
		client.close();
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
	public static class HttpResult {
		private final byte[] content;
		private final String contentType;
		private final String lastModifiedSince;
		private final String eTag;
		private final long duration;
		private final String urlAfterRedirect;
	}

}
