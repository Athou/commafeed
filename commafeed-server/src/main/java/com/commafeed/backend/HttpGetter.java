package com.commafeed.backend;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

import com.commafeed.CommaFeedConfiguration;
import com.google.common.net.HttpHeaders;

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
public class HttpGetter {

	private final HttpClient client;
	private final String userAgent;

	@Inject
	public HttpGetter(CommaFeedConfiguration config) {
		this.client = newClient();
		this.userAgent = Optional.ofNullable(config.getApplicationSettings().getUserAgent())
				.orElseGet(() -> String.format("CommaFeed/%s (https://github.com/Athou/commafeed)", config.getVersion()));
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

		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.timeout(Duration.ofMillis(timeout))
				.header(HttpHeaders.ACCEPT_LANGUAGE, "en")
				.header(HttpHeaders.PRAGMA, "No-cache")
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.header(HttpHeaders.USER_AGENT, userAgent);
		if (lastModified != null) {
			builder.header(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
		}
		if (eTag != null) {
			builder.header(HttpHeaders.IF_NONE_MATCH, eTag);
		}
		HttpRequest request = builder.GET().build();

		HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		int code = response.statusCode();
		if (code == HttpStatus.NOT_MODIFIED_304) {
			throw new NotModifiedException("'304 - not modified' http code received");
		} else if (code >= 300) {
			throw new HttpResponseException(code, "Server returned HTTP error code " + code);
		}

		String lastModifiedHeader = response.headers().firstValue(HttpHeaders.LAST_MODIFIED).map(StringUtils::trimToNull).orElse(null);
		if (lastModifiedHeader != null && lastModifiedHeader.equals(lastModified)) {
			throw new NotModifiedException("lastModifiedHeader is the same");
		}

		String eTagHeader = response.headers().firstValue(HttpHeaders.ETAG).map(StringUtils::trimToNull).orElse(null);
		if (eTagHeader != null && eTagHeader.equals(eTag)) {
			throw new NotModifiedException("eTagHeader is the same");
		}

		byte[] content = response.body();
		String contentType = response.headers().firstValue(HttpHeaders.CONTENT_TYPE).orElse(null);
		String urlAfterRedirect = response.request().uri().toString();

		long duration = System.currentTimeMillis() - start;
		return new HttpResult(content, contentType, lastModifiedHeader, eTagHeader, duration, urlAfterRedirect);
	}

	private HttpClient newClient() {
		SSLFactory sslFactory = SSLFactory.builder().withUnsafeTrustMaterial().withUnsafeHostnameVerifier().build();
		return HttpClient.newBuilder()
				.version(Version.HTTP_1_1)
				.connectTimeout(Duration.ofSeconds(5))
				.followRedirects(Redirect.ALWAYS)
				.sslContext(sslFactory.getSslContext())
				.sslParameters(sslFactory.getSslParameters())
				.cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_NONE))
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
	public static class HttpResult {
		private final byte[] content;
		private final String contentType;
		private final String lastModifiedSince;
		private final String eTag;
		private final long duration;
		private final String urlAfterRedirect;
	}

}
