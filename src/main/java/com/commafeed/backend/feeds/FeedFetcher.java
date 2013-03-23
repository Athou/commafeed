package com.commafeed.backend.feeds;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.model.Feed;

@Singleton
public class FeedFetcher {

	private static Logger log = LoggerFactory.getLogger(FeedFetcher.class);

	@Inject
	FeedParser parser;

	@Asynchronous
	@Lock(LockType.READ)
	@AccessTimeout(value = 15, unit = TimeUnit.SECONDS)
	public Future<Feed> fetch(String feedUrl) {
		log.debug("Fetching feed {}", feedUrl);
		Feed feed = null;

		HttpClient httpclient = new DefaultHttpClient();
		HttpProtocolParams.setContentCharset(httpclient.getParams(), "UTF-8");

		try {
			HttpGet httpget = new HttpGet(feedUrl);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity, "UTF-8");
			feed = parser.parse(feedUrl, content);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return new AsyncResult<Feed>(feed);
	}

}
