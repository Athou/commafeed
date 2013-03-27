package com.commafeed.backend.feeds;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.model.Feed;
import com.sun.syndication.io.FeedException;

@Stateless
public class FeedFetcher {

	private static Logger log = LoggerFactory.getLogger(FeedFetcher.class);

	@Inject
	FeedParser parser;

	public Feed fetch(String feedUrl) throws FeedException {
		log.debug("Fetching feed {}", feedUrl);
		Feed feed = null;

		HttpClient httpclient = new DefaultHttpClient();
		HttpProtocolParams.setContentCharset(httpclient.getParams(), "UTF-8");
		HttpConnectionParams
				.setConnectionTimeout(httpclient.getParams(), 15000);
		HttpConnectionParams.setSoTimeout(httpclient.getParams(), 15000);

		try {
			HttpGet httpget = new HttpGet(feedUrl);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity, "UTF-8");
			feed = parser.parse(feedUrl, content);
		} catch (Exception e) {
			throw new FeedException(e.getMessage(), e);
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return feed;
	}

}
