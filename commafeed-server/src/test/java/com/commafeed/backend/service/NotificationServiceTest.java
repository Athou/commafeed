package com.commafeed.backend.service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings.NotificationType;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private HttpClient httpClient;

	@Mock
	private HttpResponse<String> httpResponse;

	private NotificationService notificationService;

	@BeforeEach
	void setUp() {
		notificationService = new NotificationService(httpClient);
	}

	private void stubHttpClient() throws Exception {
		Mockito.when(httpResponse.statusCode()).thenReturn(200);
		Mockito.when(httpClient.send(Mockito.any(HttpRequest.class), Mockito.<BodyHandler<String>> any())).thenReturn(httpResponse);
	}

	private HttpRequest captureRequest() throws Exception {
		ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
		Mockito.verify(httpClient).send(captor.capture(), Mockito.<BodyHandler<String>> any());
		return captor.getValue();
	}

	@Test
	void sendNtfyBuildsCorrectRequest() throws Exception {
		stubHttpClient();

		UserSettings settings = newSettings(NotificationType.NTFY);
		settings.setNotificationServerUrl("https://ntfy.example.com");
		settings.setNotificationTopic("my-topic");
		settings.setNotificationToken("my-token");

		FeedSubscription sub = newSubscription("My Feed");
		FeedEntry entry = newEntry("New Article", "https://example.com/article");

		notificationService.notify(settings, sub, entry);

		HttpRequest request = captureRequest();
		Assertions.assertEquals("https://ntfy.example.com/my-topic", request.uri().toString());
		Assertions.assertEquals("My Feed: New Article", request.headers().firstValue("Title").orElse(null));
		Assertions.assertEquals("https://example.com/article", request.headers().firstValue("Click").orElse(null));
		Assertions.assertEquals("Bearer my-token", request.headers().firstValue("Authorization").orElse(null));
	}

	@Test
	void sendNtfyOmitsOptionalHeaders() throws Exception {
		stubHttpClient();

		UserSettings settings = newSettings(NotificationType.NTFY);
		settings.setNotificationServerUrl("https://ntfy.example.com");
		settings.setNotificationTopic("my-topic");

		FeedSubscription sub = newSubscription("My Feed");
		FeedEntry entry = newEntry("Title", "");

		notificationService.notify(settings, sub, entry);

		HttpRequest request = captureRequest();
		Assertions.assertTrue(request.headers().firstValue("Click").isEmpty());
		Assertions.assertTrue(request.headers().firstValue("Authorization").isEmpty());
	}

	@Test
	void sendNtfySkipsWhenMissingConfig() throws Exception {
		UserSettings settings = newSettings(NotificationType.NTFY);
		settings.setNotificationTopic("topic");
		notificationService.notify(settings, newSubscription("F"), newEntry("T", "U"));
		Mockito.verify(httpClient, Mockito.never()).send(Mockito.any(), Mockito.any());

		UserSettings settings2 = newSettings(NotificationType.NTFY);
		settings2.setNotificationServerUrl("https://ntfy.example.com");
		notificationService.notify(settings2, newSubscription("F"), newEntry("T", "U"));
		Mockito.verify(httpClient, Mockito.never()).send(Mockito.any(), Mockito.any());
	}

	@Test
	void sendGotifyBuildsCorrectRequest() throws Exception {
		stubHttpClient();

		UserSettings settings = newSettings(NotificationType.GOTIFY);
		settings.setNotificationServerUrl("https://gotify.example.com/");
		settings.setNotificationToken("app-token");

		FeedSubscription sub = newSubscription("My Feed");
		FeedEntry entry = newEntry("New Article", "https://example.com/article");

		notificationService.notify(settings, sub, entry);

		HttpRequest request = captureRequest();
		Assertions.assertEquals("https://gotify.example.com/message", request.uri().toString());
		Assertions.assertEquals("app-token", request.headers().firstValue("X-Gotify-Key").orElse(null));
		Assertions.assertEquals("application/json", request.headers().firstValue("Content-Type").orElse(null));
	}

	@Test
	void sendGotifySkipsWhenMissingConfig() throws Exception {
		UserSettings settings = newSettings(NotificationType.GOTIFY);
		settings.setNotificationToken("token");
		notificationService.notify(settings, newSubscription("F"), newEntry("T", "U"));
		Mockito.verify(httpClient, Mockito.never()).send(Mockito.any(), Mockito.any());

		UserSettings settings2 = newSettings(NotificationType.GOTIFY);
		settings2.setNotificationServerUrl("https://gotify.example.com");
		notificationService.notify(settings2, newSubscription("F"), newEntry("T", "U"));
		Mockito.verify(httpClient, Mockito.never()).send(Mockito.any(), Mockito.any());
	}

	@Test
	void sendPushoverBuildsCorrectRequest() throws Exception {
		stubHttpClient();

		UserSettings settings = newSettings(NotificationType.PUSHOVER);
		settings.setNotificationToken("po-token");
		settings.setNotificationUserKey("po-user");

		FeedSubscription sub = newSubscription("My Feed");
		FeedEntry entry = newEntry("New Article", "https://example.com/article");

		notificationService.notify(settings, sub, entry);

		HttpRequest request = captureRequest();
		Assertions.assertEquals("https://api.pushover.net/1/messages.json", request.uri().toString());
		Assertions.assertEquals("application/x-www-form-urlencoded", request.headers().firstValue("Content-Type").orElse(null));
	}

	@Test
	void sendPushoverOmitsUrlWhenBlank() throws Exception {
		stubHttpClient();

		UserSettings settings = newSettings(NotificationType.PUSHOVER);
		settings.setNotificationToken("po-token");
		settings.setNotificationUserKey("po-user");

		FeedSubscription sub = newSubscription("My Feed");
		FeedEntry entry = newEntry("Title", "");

		notificationService.notify(settings, sub, entry);

		Mockito.verify(httpClient).send(Mockito.any(HttpRequest.class), Mockito.<BodyHandler<String>> any());
	}

	@Test
	void sendPushoverSkipsWhenMissingConfig() throws Exception {
		UserSettings settings = newSettings(NotificationType.PUSHOVER);
		settings.setNotificationUserKey("user");
		notificationService.notify(settings, newSubscription("F"), newEntry("T", "U"));
		Mockito.verify(httpClient, Mockito.never()).send(Mockito.any(), Mockito.any());

		UserSettings settings2 = newSettings(NotificationType.PUSHOVER);
		settings2.setNotificationToken("token");
		notificationService.notify(settings2, newSubscription("F"), newEntry("T", "U"));
		Mockito.verify(httpClient, Mockito.never()).send(Mockito.any(), Mockito.any());
	}

	@Test
	void notifyDoesNotPropagateExceptions() throws Exception {
		Mockito.when(httpClient.send(Mockito.any(HttpRequest.class), Mockito.<BodyHandler<String>> any()))
				.thenThrow(new IOException("connection failed"));

		UserSettings settings = newSettings(NotificationType.NTFY);
		settings.setNotificationServerUrl("https://ntfy.example.com");
		settings.setNotificationTopic("topic");

		Assertions.assertDoesNotThrow(() -> notificationService.notify(settings, newSubscription("Feed"), newEntry("Title", "url")));
	}

	@Test
	void notifyUsesNewEntryAsFallbackTitle() throws Exception {
		stubHttpClient();

		UserSettings settings = newSettings(NotificationType.NTFY);
		settings.setNotificationServerUrl("https://ntfy.example.com");
		settings.setNotificationTopic("topic");

		FeedSubscription sub = newSubscription("Feed");

		FeedEntry entryNoContent = new FeedEntry();
		entryNoContent.setUrl("https://example.com");
		notificationService.notify(settings, sub, entryNoContent);

		HttpRequest request = captureRequest();
		Assertions.assertEquals("Feed: New entry", request.headers().firstValue("Title").orElse(null));
	}

	private UserSettings newSettings(NotificationType type) {
		UserSettings settings = new UserSettings();
		settings.setNotificationEnabled(true);
		settings.setNotificationType(type);
		return settings;
	}

	private FeedSubscription newSubscription(String title) {
		FeedSubscription sub = new FeedSubscription();
		sub.setTitle(title);
		return sub;
	}

	private FeedEntry newEntry(String title, String url) {
		FeedEntryContent content = new FeedEntryContent();
		content.setTitle(title);
		FeedEntry entry = new FeedEntry();
		entry.setContent(content);
		entry.setUrl(url);
		return entry;
	}
}
