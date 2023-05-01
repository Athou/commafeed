package com.commafeed.backend;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.commafeed.CommaFeedConfiguration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.altindag.ssl.SSLFactory;

/**
 * Smart HTTP getter: handles gzip, ssl, last modified and etag headers
 *
 */
@Singleton
public class HttpGetter {

	private static final String ACCEPT_LANGUAGE = "en";
	private static final String PRAGMA_NO_CACHE = "No-cache";
	private static final String CACHE_CONTROL_NO_CACHE = "no-cache";

	private static final HttpResponseInterceptor REMOVE_INCORRECT_CONTENT_ENCODING = new ContentEncodingInterceptor();

	private static final SSLFactory SSL_FACTORY = SSLFactory.builder().withUnsafeTrustMaterial().withUnsafeHostnameVerifier().build();

	private String userAgent;

	@Inject
	public HttpGetter(CommaFeedConfiguration config) {
		this.userAgent = config.getApplicationSettings().getUserAgent();
		if (this.userAgent == null) {
			this.userAgent = String.format("CommaFeed/%s (https://github.com/Athou/commafeed)", config.getVersion());
		}
	}

	public HttpResult getBinary(String url, int timeout) throws IOException, NotModifiedException {
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
	public HttpResult getBinary(String url, String lastModified, String eTag, int timeout) throws IOException, NotModifiedException {
		HttpResult result = null;
		long start = System.currentTimeMillis();

		CloseableHttpClient client = newClient(timeout);
		CloseableHttpResponse response = null;
		try {
			HttpGet httpget = new HttpGet(url);
			HttpClientContext context = HttpClientContext.create();

			httpget.addHeader(HttpHeaders.ACCEPT_LANGUAGE, ACCEPT_LANGUAGE);
			httpget.addHeader(HttpHeaders.PRAGMA, PRAGMA_NO_CACHE);
			httpget.addHeader(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_NO_CACHE);
			httpget.addHeader(HttpHeaders.USER_AGENT, userAgent);

			if (lastModified != null) {
				httpget.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
			}
			if (eTag != null) {
				httpget.addHeader(HttpHeaders.IF_NONE_MATCH, eTag);
			}

			try {
				response = client.execute(httpget, context);
				int code = response.getStatusLine().getStatusCode();
				if (code == HttpStatus.SC_NOT_MODIFIED) {
					throw new NotModifiedException("'304 - not modified' http code received");
				} else if (code >= 300) {
					throw new HttpResponseException(code, "Server returned HTTP error code " + code);
				}

			} catch (HttpResponseException e) {
				if (e.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
					throw new NotModifiedException("'304 - not modified' http code received");
				} else {
					throw e;
				}
			}
			Header lastModifiedHeader = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);
			String lastModifiedHeaderValue = lastModifiedHeader == null ? null : StringUtils.trimToNull(lastModifiedHeader.getValue());
			if (lastModifiedHeaderValue != null && StringUtils.equals(lastModified, lastModifiedHeaderValue)) {
				throw new NotModifiedException("lastModifiedHeader is the same");
			}

			Header eTagHeader = response.getFirstHeader(HttpHeaders.ETAG);
			String eTagHeaderValue = eTagHeader == null ? null : StringUtils.trimToNull(eTagHeader.getValue());
			if (eTag != null && StringUtils.equals(eTag, eTagHeaderValue)) {
				throw new NotModifiedException("eTagHeader is the same");
			}

			HttpEntity entity = response.getEntity();
			byte[] content = null;
			String contentType = null;
			if (entity != null) {
				content = EntityUtils.toByteArray(entity);
				if (entity.getContentType() != null) {
					contentType = entity.getContentType().getValue();
				}
			}

			String urlAfterRedirect = url;
			if (context.getRequest() instanceof HttpUriRequest) {
				HttpUriRequest req = (HttpUriRequest) context.getRequest();
				HttpHost host = context.getTargetHost();
				urlAfterRedirect = req.getURI().isAbsolute() ? req.getURI().toString() : host.toURI() + req.getURI();
			}

			long duration = System.currentTimeMillis() - start;
			result = new HttpResult(content, contentType, lastModifiedHeaderValue, eTagHeaderValue, duration, urlAfterRedirect);
		} finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(client);
		}
		return result;
	}

	public static CloseableHttpClient newClient(int timeout) {
		HttpClientBuilder builder = HttpClients.custom();
		builder.useSystemProperties();
		builder.addInterceptorFirst(REMOVE_INCORRECT_CONTENT_ENCODING);
		builder.disableAutomaticRetries();

		builder.setSSLContext(SSL_FACTORY.getSslContext());
		builder.setSSLHostnameVerifier(SSL_FACTORY.getHostnameVerifier());

		RequestConfig.Builder configBuilder = RequestConfig.custom();
		configBuilder.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
		configBuilder.setSocketTimeout(timeout);
		configBuilder.setConnectTimeout(timeout);
		configBuilder.setConnectionRequestTimeout(timeout);
		builder.setDefaultRequestConfig(configBuilder.build());

		builder.setDefaultConnectionConfig(ConnectionConfig.custom().setCharset(Consts.ISO_8859_1).build());

		return builder.build();
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
