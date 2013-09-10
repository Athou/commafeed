package org.baeldung.live;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Original source code from
 * http://www.baeldung.com/unshorten-url-httpclient
 * https://github.com/eugenp/tutorials/blob/master/spring-security-rest-custom/src/test/java/org/baeldung/live/HttpLiveServiceTemp.java
 * 
 * @author dersteppenwolf
 * 
 */
public class UrlUnshortenerUtil {

	private static HttpParams httpParameters ;
	

	// fixtures

	static {
		httpParameters = new BasicHttpParams();
		httpParameters.setParameter("http.protocol.handle-redirects", false);
	}

	// API

	public final static String expand(final String urlArg)  {
		String originalUrl = urlArg;
		String newUrl;
		try {
			newUrl = expandSingleLevel(originalUrl);
			while (!originalUrl.equals(newUrl)) {
				originalUrl = newUrl;
				newUrl = expandSingleLevel(originalUrl);
			}
		} catch (IOException e) {
			newUrl = urlArg;
		}
		return newUrl;
	}

	public final static String expandSafe(final String urlArg) throws IOException {
		String originalUrl = urlArg;
		String newUrl = expandSingleLevelSafe(originalUrl).getRight();
		final List<String> alreadyVisited = Lists.newArrayList(originalUrl,
				newUrl);
		while (!originalUrl.equals(newUrl)) {
			originalUrl = newUrl;
			final Pair<Integer, String> statusAndUrl = expandSingleLevelSafe(originalUrl);
			newUrl = statusAndUrl.getRight();
			final boolean isRedirect = statusAndUrl.getLeft() == 301
					|| statusAndUrl.getLeft() == 302;
			if (isRedirect && alreadyVisited.contains(newUrl)) {
				throw new IllegalStateException("Likely a redirect loop");
			}
			alreadyVisited.add(newUrl);
		}

		return newUrl;
	}

	public final static Pair<Integer, String> expandSingleLevelSafe(final String url)
			throws IOException {
		HttpGet request = null;
		HttpEntity httpEntity = null;
		InputStream entityContentStream = null;

		try {
			DefaultHttpClient client = new DefaultHttpClient(httpParameters);
			
			request = new HttpGet(url);
			
			final HttpResponse httpResponse = client.execute(request);

			httpEntity = httpResponse.getEntity();
			entityContentStream = httpEntity.getContent();

			final int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode != 301 && statusCode != 302) {
				return new ImmutablePair<Integer, String>(statusCode, url);
			}
			final Header[] headers = httpResponse
					.getHeaders(HttpHeaders.LOCATION);
			Preconditions.checkState(headers.length == 1);
			final String newUrl = headers[0].getValue();

			return new ImmutablePair<Integer, String>(statusCode, newUrl);
		} catch (final IllegalArgumentException uriEx) {
			return new ImmutablePair<Integer, String>(500, url);
		} finally {
			if (request != null) {
				request.releaseConnection();
			}
			if (entityContentStream != null) {
				entityContentStream.close();
			}
			if (httpEntity != null) {
				EntityUtils.consume(httpEntity);
			}
		}
	}

	public final static String expandSingleLevel(final String url) throws IOException {
		HttpGet request = null;
		HttpEntity httpEntity = null;
		InputStream entityContentStream = null;

		try {
			DefaultHttpClient client = new DefaultHttpClient(httpParameters);
			request = new HttpGet(url);
			final HttpResponse httpResponse = client.execute(request);

			httpEntity = httpResponse.getEntity();
			entityContentStream = httpEntity.getContent();

			final int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode != 301 && statusCode != 302) {
				return url;
			}
			final Header[] headers = httpResponse.getHeaders(HttpHeaders.LOCATION);
			Preconditions.checkState(headers.length == 1);
			final String newUrl = headers[0].getValue();
			return newUrl;
		} catch (final IllegalArgumentException uriEx) {
			return url;
		} finally {
			if (request != null) {
				request.releaseConnection();
			}
			if (entityContentStream != null) {
				entityContentStream.close();
			}
			if (httpEntity != null) {
				EntityUtils.consume(httpEntity);
			}
		}
	}

}
