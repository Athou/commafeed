package com.commafeed.backend.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.feed.FeedQueues;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.frontend.resource.PubSubHubbubCallbackREST;

/**
 * Sends push subscription requests. Callback is handled by {@link PubSubHubbubCallbackREST}
 * 
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class PubSubService {

	private final CommaFeedConfiguration config;
	private final FeedQueues queues;

	public void subscribe(Feed feed) {

		try {
			// make sure the feed has been updated in the database so that the
			// callback works
			Thread.sleep(30000);
		} catch (InterruptedException e1) {
			// do nothing
		}

		String hub = feed.getPushHub();
		String topic = feed.getPushTopic();
		String publicUrl = FeedUtils.removeTrailingSlash(config.getApplicationSettings().getPublicUrl());

		log.debug("sending new pubsub subscription to {} for {}", hub, topic);

		HttpPost post = new HttpPost(hub);
		List<NameValuePair> nvp = new ArrayList<>();
		nvp.add(new BasicNameValuePair("hub.callback", publicUrl + "/rest/push/callback"));
		nvp.add(new BasicNameValuePair("hub.topic", topic));
		nvp.add(new BasicNameValuePair("hub.mode", "subscribe"));
		nvp.add(new BasicNameValuePair("hub.verify", "async"));
		nvp.add(new BasicNameValuePair("hub.secret", ""));
		nvp.add(new BasicNameValuePair("hub.verify_token", ""));
		nvp.add(new BasicNameValuePair("hub.lease_seconds", ""));

		post.setHeader(HttpHeaders.USER_AGENT, "CommaFeed");
		post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);

		CloseableHttpClient client = HttpGetter.newClient(20000);
		CloseableHttpResponse response = null;
		try {
			post.setEntity(new UrlEncodedFormEntity(nvp));
			response = client.execute(post);

			int code = response.getStatusLine().getStatusCode();
			if (code != 204 && code != 202 && code != 200) {
				String message = EntityUtils.toString(response.getEntity());
				String pushpressError = " is value is not allowed.  You may only subscribe to";
				if (code == 400 && StringUtils.contains(message, pushpressError)) {
					String[] tokens = message.split(" ");
					feed.setPushTopic(tokens[tokens.length - 1]);
					queues.giveBack(feed);
					log.debug("handled pushpress subfeed {} : {}", topic, feed.getPushTopic());
				} else {
					throw new Exception("Unexpected response code: " + code + " " + response.getStatusLine().getReasonPhrase() + " - "
							+ message);
				}
			}
			log.debug("subscribed to {} for {}", hub, topic);
		} catch (Exception e) {
			log.error("Could not subscribe to {} for {} : " + e.getMessage(), hub, topic);
		} finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(client);
		}
	}
}
