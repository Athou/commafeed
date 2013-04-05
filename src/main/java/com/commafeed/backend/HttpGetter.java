package com.commafeed.backend;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

public class HttpGetter {

	public String get(String url) throws Exception {
		return new String(getBinary(url), "UTF-8");
	}

	public byte[] getBinary(String url) throws Exception {
		byte[] content = null;

		HttpClient httpclient = new DefaultHttpClient();
		HttpProtocolParams.setContentCharset(httpclient.getParams(), "UTF-8");
		HttpConnectionParams
				.setConnectionTimeout(httpclient.getParams(), 15000);
		HttpConnectionParams.setSoTimeout(httpclient.getParams(), 15000);

		try {
			HttpGet httpget = new HttpGet(url);
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
