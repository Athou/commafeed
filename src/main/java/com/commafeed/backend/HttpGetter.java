package com.commafeed.backend;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

public class HttpGetter {

	public HttpResult getBinary(String url) throws ClientProtocolException,
			IOException, NotModifiedException {
		return getBinary(url, null, null);
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
	public HttpResult getBinary(String url, String lastModified, String eTag)
			throws ClientProtocolException, IOException, NotModifiedException {
		HttpResult result = null;
		long start = System.currentTimeMillis();

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpParams params = httpclient.getParams();
		HttpClientParams.setCookiePolicy(params, CookiePolicy.IGNORE_COOKIES);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpConnectionParams.setConnectionTimeout(params, 4000);
		HttpConnectionParams.setSoTimeout(params, 4000);

		try {
			HttpGet httpget = new HttpGet(url);
			httpget.addHeader("Pragma", "No-cache");
			httpget.addHeader("Cache-Control", "no-cache");

			if (lastModified != null) {
				httpget.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
			}
			if (eTag != null) {
				httpget.addHeader(HttpHeaders.IF_NONE_MATCH, eTag);
			}

			HttpResponse response = null;
			try {
				response = httpclient.execute(httpget);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
					throw new NotModifiedException();
				}
			} catch (HttpResponseException e) {
				if (e.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
					throw new NotModifiedException();
				}
			}
			Header lastModifiedHeader = response
					.getFirstHeader(HttpHeaders.LAST_MODIFIED);
			Header eTagHeader = response.getFirstHeader(HttpHeaders.ETAG);
			HttpEntity entity = response.getEntity();

			String lastModifiedResponse = lastModifiedHeader == null ? null
					: lastModifiedHeader.getValue();
			String eTagResponse = eTagHeader == null ? null : eTagHeader
					.getValue();

			if (lastModified != null
					&& StringUtils.equals(lastModified, lastModifiedResponse)) {
				throw new NotModifiedException();
			}

			if (eTag != null && StringUtils.equals(eTag, eTagResponse)) {
				throw new NotModifiedException();
			}

			byte[] content = null;
			if (entity != null) {
				content = EntityUtils.toByteArray(entity);
			}
			long duration = System.currentTimeMillis() - start;
			result = new HttpResult(content, lastModifiedHeader == null ? null
					: lastModifiedHeader.getValue(), eTagHeader == null ? null
					: eTagHeader.getValue(), duration);
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return result;
	}

	public static class HttpResult {

		private byte[] content;
		private String lastModifiedSince;
		private String eTag;
		private long duration;

		public HttpResult(byte[] content, String lastModifiedSince,
				String eTag, long duration) {
			this.content = content;
			this.lastModifiedSince = lastModifiedSince;
			this.eTag = eTag;
			this.duration = duration;
		}

		public byte[] getContent() {
			return content;
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

	}

	public static class NotModifiedException extends Exception {
		private static final long serialVersionUID = 1L;

	}
}
