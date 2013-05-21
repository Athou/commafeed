package com.commafeed.backend.pubsubhubbub;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedPushInfo;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.google.common.collect.Lists;

public class SubscriptionHandler {

	private static Logger log = LoggerFactory
			.getLogger(SubscriptionHandler.class);

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public void subscribe(Feed feed) {
		FeedPushInfo info = feed.getPushInfo();
		String hub = info.getHub();
		String topic = info.getTopic();
		String publicUrl = applicationSettingsService.get().getPublicUrl();
		if (publicUrl.endsWith("/")) {
			publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
		}

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
			if (code != 204 && code != 202) {
				throw new Exception("Unexpected response code: " + code + " "
						+ response.getStatusLine().getReasonPhrase());
			}
			log.debug("subscribed to {} for {}", hub, topic);
		} catch (Exception e) {
			log.error("Could not subscribe to {} for {} : {}", hub, topic,
					e.getMessage(), e);
		} finally {
			client.getConnectionManager().shutdown();
		}
	}
}
