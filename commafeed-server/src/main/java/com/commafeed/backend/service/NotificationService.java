package com.commafeed.backend.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.UserSettings;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class NotificationService {

	private final HttpClient httpClient;

	public NotificationService() {
		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	}

	public NotificationService(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void notify(UserSettings settings, FeedSubscription subscription, FeedEntry entry) {
		if (!settings.isNotificationEnabled() || settings.getNotificationType() == null) {
			return;
		}

		String entryTitle = entry.getContent() != null ? entry.getContent().getTitle() : null;
		String entryUrl = entry.getUrl();
		String feedTitle = subscription.getTitle();

		if (StringUtils.isBlank(entryTitle)) {
			entryTitle = "New entry";
		}

		try {
			switch (settings.getNotificationType()) {
			case NTFY -> sendNtfy(settings, feedTitle, entryTitle, entryUrl);
			case GOTIFY -> sendGotify(settings, feedTitle, entryTitle, entryUrl);
			case PUSHOVER -> sendPushover(settings, feedTitle, entryTitle, entryUrl);
			default -> log.warn("unknown notification type: {}", settings.getNotificationType());
			}
		} catch (Exception e) {
			log.error("failed to send {} notification for entry '{}' in feed '{}'", settings.getNotificationType(), entryTitle, feedTitle,
					e);
		}
	}

	private void sendNtfy(UserSettings settings, String feedTitle, String entryTitle, String entryUrl) throws Exception {
		String serverUrl = stripTrailingSlash(settings.getNotificationServerUrl());
		String topic = settings.getNotificationTopic();

		if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(topic)) {
			log.warn("ntfy notification skipped: missing server URL or topic");
			return;
		}

		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(serverUrl + "/" + topic))
				.timeout(Duration.ofSeconds(10))
				.header("Title", feedTitle + ": " + entryTitle)
				.POST(BodyPublishers.ofString(entryTitle));

		if (StringUtils.isNotBlank(entryUrl)) {
			builder.header("Click", entryUrl);
		}

		if (StringUtils.isNotBlank(settings.getNotificationToken())) {
			builder.header("Authorization", "Bearer " + settings.getNotificationToken());
		}

		HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() >= 400) {
			log.error("ntfy notification failed with status {}: {}", response.statusCode(), response.body());
		}
	}

	private void sendGotify(UserSettings settings, String feedTitle, String entryTitle, String entryUrl) throws Exception {
		String serverUrl = stripTrailingSlash(settings.getNotificationServerUrl());
		String token = settings.getNotificationToken();

		if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(token)) {
			log.warn("gotify notification skipped: missing server URL or token");
			return;
		}

		String message = entryTitle;
		if (StringUtils.isNotBlank(entryUrl)) {
			message += "\n" + entryUrl;
		}

		String json = """
				{"title":"%s","message":"%s","priority":5,"extras":{"client::notification":{"click":{"url":"%s"}}}}"""
				.formatted(escapeJson(feedTitle), escapeJson(message), escapeJson(StringUtils.defaultString(entryUrl)));

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(serverUrl + "/message"))
				.timeout(Duration.ofSeconds(10))
				.header("Content-Type", "application/json")
				.header("X-Gotify-Key", token)
				.POST(BodyPublishers.ofString(json))
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() >= 400) {
			log.error("gotify notification failed with status {}: {}", response.statusCode(), response.body());
		}
	}

	private void sendPushover(UserSettings settings, String feedTitle, String entryTitle, String entryUrl) throws Exception {
		String token = settings.getNotificationToken();
		String userKey = settings.getNotificationUserKey();

		if (StringUtils.isBlank(token) || StringUtils.isBlank(userKey)) {
			log.warn("pushover notification skipped: missing token or user key");
			return;
		}

		StringBuilder body = new StringBuilder();
		body.append("token=").append(urlEncode(token));
		body.append("&user=").append(urlEncode(userKey));
		body.append("&title=").append(urlEncode(feedTitle));
		body.append("&message=").append(urlEncode(entryTitle));
		if (StringUtils.isNotBlank(entryUrl)) {
			body.append("&url=").append(urlEncode(entryUrl));
		}

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.pushover.net/1/messages.json"))
				.timeout(Duration.ofSeconds(10))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(BodyPublishers.ofString(body.toString()))
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() >= 400) {
			log.error("pushover notification failed with status {}: {}", response.statusCode(), response.body());
		}
	}

	private static String stripTrailingSlash(String url) {
		if (url != null && url.endsWith("/")) {
			return url.substring(0, url.length() - 1);
		}
		return url;
	}

	private static String urlEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}
}
