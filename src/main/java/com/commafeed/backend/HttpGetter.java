package com.commafeed.backend;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

public class HttpGetter {

	public String get(String url) throws Exception {
		return new String(getBinary(url), "UTF-8");
	}

	public byte[] getBinary(String url) throws ClientProtocolException,
			IOException {
		byte[] content = null;

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
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				content = EntityUtils.toByteArray(entity);
			}
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return content;
	}
}
