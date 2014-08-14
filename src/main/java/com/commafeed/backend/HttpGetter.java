package com.commafeed.backend;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * Smart HTTP getter: handles gzip, ssl, last modified and etag headers
 * 
 */
@Slf4j
public class HttpGetter {

	private static final String USER_AGENT = "CommaFeed/1.0 (http://www.commafeed.com)";
	private static final String ACCEPT_LANGUAGE = "en";
	private static final String PRAGMA_NO_CACHE = "No-cache";
	private static final String CACHE_CONTROL_NO_CACHE = "no-cache";

	private static final List<String> ALLOWED_CONTENT_ENCODINGS = Arrays.asList("gzip", "x-gzip", "deflate", "identity");
	private static final HttpResponseInterceptor REMOVE_INCORRECT_CONTENT_ENCODING = new HttpResponseInterceptor() {

		@Override
		public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
			HttpEntity entity = response.getEntity();
			if (entity != null && entity.getContentLength() != 0) {
				Header header = entity.getContentEncoding();
				if (header != null) {
					HeaderElement[] codecs = header.getElements();
					for (final HeaderElement codec : codecs) {
						String codecName = codec.getName().toLowerCase(Locale.US);
						if (!ALLOWED_CONTENT_ENCODINGS.contains(codecName)) {
							response.setEntity(new HttpEntityWrapper(entity) {
								@Override
								public Header getContentEncoding() {
									return null;
								};
							});
						}
					}
				}
			}
		}
	};

	private static SSLContext SSL_CONTEXT = null;
	static {
		try {
			SSL_CONTEXT = SSLContext.getInstance("TLS");
			SSL_CONTEXT.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
		} catch (Exception e) {
			log.error("Could not configure ssl context");
		}
	}

	public HttpResult getBinary(String url, int timeout) throws ClientProtocolException, IOException, NotModifiedException {
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
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws NotModifiedException
	 *             if the url hasn't changed since we asked for it last time
	 */
	public HttpResult getBinary(String url, String lastModified, String eTag, int timeout) throws ClientProtocolException, IOException,
			NotModifiedException {
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
			httpget.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);

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
			HttpUriRequest req = (HttpUriRequest) context.getRequest();
			HttpHost host = context.getTargetHost();
			String urlAfterRedirect = req.getURI().isAbsolute() ? req.getURI().toString() : host.toURI() + req.getURI();

			long duration = System.currentTimeMillis() - start;
			result = new HttpResult(content, contentType, lastModifiedHeaderValue, eTagHeaderValue, duration, urlAfterRedirect);
		} finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(client);
		}
		return result;
	}

	public static class HttpResult {

		private byte[] content;
		private String contentType;
		private String lastModifiedSince;
		private String eTag;
		private long duration;
		private String urlAfterRedirect;

		private HttpResult(byte[] content, String contentType, String lastModifiedSince, String eTag, long duration, String urlAfterRedirect) {
			this.content = content;
			this.contentType = contentType;
			this.lastModifiedSince = lastModifiedSince;
			this.eTag = eTag;
			this.duration = duration;
			this.urlAfterRedirect = urlAfterRedirect;
		}

		public byte[] getContent() {
			return content;
		}

		public String getContentType() {
			return contentType;
		}

		public String getLastModifiedSince() {
			return lastModifiedSince;
		}

		public String geteTag() {
			return eTag;
		}

		public long getDuration() {
			return duration;
		}

		public String getUrlAfterRedirect() {
			return urlAfterRedirect;
		}
	}

	public static CloseableHttpClient newClient(int timeout) {
		HttpClientBuilder builder = HttpClients.custom();
		builder.useSystemProperties();
		builder.addInterceptorFirst(REMOVE_INCORRECT_CONTENT_ENCODING);
		builder.disableAutomaticRetries();

		builder.setSslcontext(SSL_CONTEXT);
		builder.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		RequestConfig.Builder configBuilder = RequestConfig.custom();
		configBuilder.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
		configBuilder.setSocketTimeout(timeout);
		configBuilder.setConnectTimeout(timeout);
		configBuilder.setConnectionRequestTimeout(timeout);
		builder.setDefaultRequestConfig(configBuilder.build());

		builder.setDefaultConnectionConfig(ConnectionConfig.custom().setCharset(Consts.ISO_8859_1).build());

		return builder.build();
	}

	public static class NotModifiedException extends Exception {
		private static final long serialVersionUID = 1L;

		public NotModifiedException(String message) {
			super(message);
		}

	}

	private static class DefaultTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
}
