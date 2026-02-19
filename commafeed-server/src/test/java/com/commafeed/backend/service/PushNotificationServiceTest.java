package com.commafeed.backend.service;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpClientFactory;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.UserSettings.PushNotificationType;
import com.commafeed.backend.model.UserSettings.PushNotificationUserSettings;

@ExtendWith(MockServerExtension.class)
class PushNotificationServiceTest {

	private MockServerClient mockServerClient;
	private PushNotificationService pushNotificationService;
	private CommaFeedConfiguration config;
	private PushNotificationUserSettings userSettings;
	private FeedSubscription subscription;
	private FeedEntry entry;

	@BeforeEach
	void init(MockServerClient mockServerClient) {
		this.mockServerClient = mockServerClient;
		this.mockServerClient.reset();

		this.config = Mockito.mock(CommaFeedConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.when(config.pushNotifications().enabled()).thenReturn(true);
		Mockito.when(config.pushNotifications().threads()).thenReturn(1);
		Mockito.when(config.httpClient().responseTimeout()).thenReturn(Duration.ofSeconds(30));

		HttpClientFactory httpClientFactory = new HttpClientFactory(config, Mockito.mock(com.commafeed.CommaFeedVersion.class));
		MetricRegistry metricRegistry = Mockito.mock(MetricRegistry.class);
		Mockito.when(metricRegistry.meter(Mockito.anyString())).thenReturn(Mockito.mock(Meter.class));

		this.pushNotificationService = new PushNotificationService(httpClientFactory, metricRegistry, config);

		this.userSettings = new PushNotificationUserSettings();

		this.subscription = createSubscription("Test Feed");
		this.entry = createEntry("Test Entry", "http://example.com/entry");
	}

	@Test
	void testNtfyNotification() {
		userSettings.setType(PushNotificationType.NTFY);
		userSettings.setServerUrl("http://localhost:" + mockServerClient.getPort());
		userSettings.setTopic("test-topic");
		userSettings.setUserSecret("test-secret");

		mockServerClient.when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/test-topic")
				.withHeader("Title", "Test Feed")
				.withHeader("Click", "http://example.com/entry")
				.withHeader("Authorization", "Bearer test-secret")
				.withBody("Test Entry")).respond(HttpResponse.response().withStatusCode(200));

		Assertions.assertDoesNotThrow(() -> pushNotificationService.notify(userSettings, subscription, entry));
	}

	@Test
	void testGotifyNotification() {
		userSettings.setType(PushNotificationType.GOTIFY);
		userSettings.setServerUrl("http://localhost:" + mockServerClient.getPort());
		userSettings.setUserSecret("gotify-token");

		mockServerClient.when(HttpRequest.request()
				.withMethod("POST")
				.withPath("/message")
				.withHeader("X-Gotify-Key", "gotify-token")
				.withContentType(MediaType.APPLICATION_JSON_UTF_8)
				.withBody(JsonBody.json("""
						{
							"title": "Test Feed",
							"message": "Test Entry",
							"priority": 5,
							"extras": {
								"client::notification": {
									"click": {
										"url": "http://example.com/entry"
									}
								}
							}
						}
						"""))).respond(HttpResponse.response().withStatusCode(200));

		Assertions.assertDoesNotThrow(() -> pushNotificationService.notify(userSettings, subscription, entry));
	}

	@Test
	void testPushNotificationDisabled() {
		Mockito.when(config.pushNotifications().enabled()).thenReturn(false);
		userSettings.setType(PushNotificationType.NTFY);
		userSettings.setServerUrl("http://localhost:" + mockServerClient.getPort());
		userSettings.setTopic("test-topic");

		Assertions.assertDoesNotThrow(() -> pushNotificationService.notify(userSettings, subscription, entry));
		mockServerClient.verifyZeroInteractions();
	}

	private static FeedSubscription createSubscription(String title) {
		FeedSubscription subscription = new FeedSubscription();
		subscription.setTitle(title);
		subscription.setFeed(new Feed());

		return subscription;
	}

	private static FeedEntry createEntry(String title, String url) {
		FeedEntry entry = new FeedEntry();

		FeedEntryContent content = new FeedEntryContent();
		content.setTitle(title);

		entry.setContent(content);
		entry.setUrl(url);
		return entry;
	}
}
