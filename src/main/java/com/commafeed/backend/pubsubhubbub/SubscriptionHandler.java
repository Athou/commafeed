package com.commafeed.backend.pubsubhubbub;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.google.common.collect.Lists;

public class SubscriptionHandler {

	private static Logger log = LoggerFactory
			.getLogger(SubscriptionHandler.class);

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	FeedDAO feedDAO;

	public void subscribe(Feed feed) {
		String hub = feed.getPushHub();
		String topic = feed.getPushTopic();
		String publicUrl = FeedUtils
				.removeTrailingSlash(applicationSettingsService.get()
						.getPublicUrl());

		log.debug("sending new pubsub subscription to {} for {}", hub, topic);

		HttpPost post = new HttpPost(hub);
		List<NameValuePair> nvp = Lists.newArrayList();
		nvp.add(new BasicNameValuePair("hub.callback", publicUrl
				+ "/rest/push/callback"));
		nvp.add(new BasicNameValuePair("hub.topic", topic));
		nvp.add(new BasicNameValuePair("hub.mode", "subscribe"));
		nvp.add(new BasicNameValuePair("hub.verify", "async"));
		nvp.add(new BasicNameValuePair("hub.secret", ""));
		nvp.add(new BasicNameValuePair("hub.verify_token", ""));
		nvp.add(new BasicNameValuePair("hub.lease_seconds", ""));

		post.setHeader(HttpHeaders.USER_AGENT, "CommaFeed");
		post.setHeader(HttpHeaders.CONTENT_TYPE,
				MediaType.APPLICATION_FORM_URLENCODED);

		HttpClient client = HttpGetter.newClient();
		try {
			post.setEntity(new UrlEncodedFormEntity(nvp));
			HttpResponse response = client.execute(post);

			int code = response.getStatusLine().getStatusCode();
			if (code != 204 && code != 202 && code != 200) {
				String message = EntityUtils.toString(response.getEntity());
				String pushpressError = " is value is not allowed.  You may only subscribe to";
				if (code == 400
						&& StringUtils.contains(message, pushpressError)) {
					String[] tokens = message.split(" ");
					feed.setPushTopic(tokens[tokens.length - 1]);
					feedDAO.saveOrUpdate(feed);
					log.debug("handled pushpress subfeed {} : {}", topic,
							feed.getPushTopic());
				} else {
					throw new Exception("Unexpected response code: " + code
							+ " " + response.getStatusLine().getReasonPhrase()
							+ " - " + message);
				}
			}
			log.debug("subscribed to {} for {}", hub, topic);
		} catch (Exception e) {
			log.error("Could not subscribe to {} for {} : " + e.getMessage(),
					hub, topic);
		} finally {
			client.getConnectionManager().shutdown();
		}
	}
}
