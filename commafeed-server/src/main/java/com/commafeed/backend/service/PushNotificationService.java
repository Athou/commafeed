package com.commafeed.backend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpClientFactory;
import com.commafeed.backend.Urls;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.UserSettings.PushNotificationUserSettings;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class PushNotificationService {

	private final CloseableHttpClient httpClient;
	private final Meter meter;
	private final CommaFeedConfiguration config;

	public PushNotificationService(HttpClientFactory httpClientFactory, MetricRegistry metrics, CommaFeedConfiguration config) {
		this.httpClient = httpClientFactory.newClient(config.pushNotifications().threads());
		this.meter = metrics.meter(MetricRegistry.name(getClass(), "notify"));
		this.config = config;
	}

	public void notify(PushNotificationUserSettings settings, FeedSubscription subscription, FeedEntry entry) {
		if (!config.pushNotifications().enabled() || settings.getType() == null) {
			return;
		}

		log.debug("sending {} push notification for entry {} in feed {}", settings.getType(), entry.getId(),
				subscription.getFeed().getId());
		String entryTitle = entry.getContent() != null ? entry.getContent().getTitle() : null;
		String entryUrl = entry.getUrl();
		String feedTitle = subscription.getTitle();

		if (StringUtils.isBlank(entryTitle)) {
			entryTitle = "New entry";
		}

		try {
			switch (settings.getType()) {
			case NTFY -> sendNtfy(settings, feedTitle, entryTitle, entryUrl);
			case GOTIFY -> sendGotify(settings, feedTitle, entryTitle, entryUrl);
			case PUSHOVER -> sendPushover(settings, feedTitle, entryTitle, entryUrl);
			default -> throw new IllegalStateException("unsupported notification type: " + settings.getType());
			}
		} catch (IOException e) {
			throw new PushNotificationException("Failed to send external notification", e);
		}

		meter.mark();
	}

	private void sendNtfy(PushNotificationUserSettings settings, String feedTitle, String entryTitle, String entryUrl) throws IOException {
		String serverUrl = Urls.removeTrailingSlash(settings.getServerUrl());
		String topic = settings.getTopic();

		if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(topic)) {
			log.warn("ntfy notification skipped: missing server URL or topic");
			return;
		}

		HttpPost request = new HttpPost(serverUrl + "/" + topic);
		request.setConfig(RequestConfig.custom().setResponseTimeout(Timeout.of(config.httpClient().responseTimeout())).build());
		request.addHeader("Title", feedTitle);
		request.setEntity(new StringEntity(entryTitle, StandardCharsets.UTF_8));

		if (StringUtils.isNotBlank(entryUrl)) {
			request.addHeader("Click", entryUrl);
		}

		if (StringUtils.isNotBlank(settings.getUserSecret())) {
			request.addHeader("Authorization", "Bearer " + settings.getUserSecret());
		}

		httpClient.execute(request, response -> {
			if (response.getCode() >= 400) {
				throw new PushNotificationException("ntfy notification failed with status " + response.getCode());
			}
			return null;
		});
	}

	private void sendGotify(PushNotificationUserSettings settings, String feedTitle, String entryTitle, String entryUrl)
			throws IOException {
		String serverUrl = Urls.removeTrailingSlash(settings.getServerUrl());
		String token = settings.getUserSecret();

		if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(token)) {
			log.warn("gotify notification skipped: missing server URL or token");
			return;
		}

		JsonObject json = new JsonObject();
		json.put("title", feedTitle);
		json.put("message", entryTitle);
		json.put("priority", 5);
		if (StringUtils.isNotBlank(entryUrl)) {
			json.put("extras",
					new JsonObject().put("client::notification", new JsonObject().put("click", new JsonObject().put("url", entryUrl))));
		}

		HttpPost request = new HttpPost(serverUrl + "/message");
		request.setConfig(RequestConfig.custom().setResponseTimeout(Timeout.of(config.httpClient().responseTimeout())).build());
		request.addHeader("X-Gotify-Key", token);
		request.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

		httpClient.execute(request, response -> {
			if (response.getCode() >= 400) {
				throw new PushNotificationException("gotify notification failed with status " + response.getCode());
			}
			return null;
		});
	}

	private void sendPushover(PushNotificationUserSettings settings, String feedTitle, String entryTitle, String entryUrl)
			throws IOException {
		String token = settings.getUserSecret();
		String userKey = settings.getUserId();

		if (StringUtils.isBlank(token) || StringUtils.isBlank(userKey)) {
			log.warn("pushover notification skipped: missing token or user key");
			return;
		}

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("token", token));
		params.add(new BasicNameValuePair("user", userKey));
		params.add(new BasicNameValuePair("title", feedTitle));
		params.add(new BasicNameValuePair("message", entryTitle));
		if (StringUtils.isNotBlank(entryUrl)) {
			params.add(new BasicNameValuePair("url", entryUrl));
		}

		HttpPost request = new HttpPost("https://api.pushover.net/1/messages.json");
		request.setConfig(RequestConfig.custom().setResponseTimeout(Timeout.of(config.httpClient().responseTimeout())).build());
		request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

		httpClient.execute(request, response -> {
			if (response.getCode() >= 400) {
				throw new PushNotificationException("pushover notification failed with status " + response.getCode());
			}
			return null;
		});
	}

	public static class PushNotificationException extends RuntimeException {
		private static final long serialVersionUID = -3392881821584833819L;

		public PushNotificationException(String message) {
			super(message);
		}

		public PushNotificationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
