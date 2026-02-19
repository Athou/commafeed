package com.commafeed.integration;

import java.io.IOException;
import java.time.Duration;

import org.apache.hc.core5.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;

import com.commafeed.TestConstants;
import com.commafeed.backend.model.UserSettings.PushNotificationType;
import com.commafeed.frontend.model.Settings;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.request.FeedModificationRequest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
class PushNotificationIT extends BaseIT {

	@BeforeEach
	void setup() {
		initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
		RestAssured.authentication = RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
	}

	@AfterEach
	void tearDown() {
		RestAssured.reset();
	}

	@Test
	void receivedPushNotifications() throws IOException {
		// mock ntfy server
		HttpRequest ntfyPost = HttpRequest.request().withMethod("POST").withPath("/ntfy/integration-test");
		getMockServerClient().when(ntfyPost).respond(HttpResponse.response().withStatusCode(200));

		// enable push notifications
		Settings settings = RestAssured.given().get("rest/user/settings").then().extract().as(Settings.class);
		settings.getPushNotificationSettings().setType(PushNotificationType.NTFY);
		settings.getPushNotificationSettings().setServerUrl("http://localhost:" + getMockServerClient().getPort() + "/ntfy");
		settings.getPushNotificationSettings().setTopic("integration-test");
		RestAssured.given().body(settings).contentType(ContentType.JSON).post("rest/user/settings").then().statusCode(200);

		// subscribe
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
		Subscription subscription = getSubscription(subscriptionId);

		// enable push notifications
		FeedModificationRequest req = new FeedModificationRequest();
		req.setId(subscriptionId);
		req.setName(subscription.getName());
		req.setCategoryId(subscription.getCategoryId());
		req.setPosition(1);
		req.setPushNotificationsEnabled(true);
		RestAssured.given().body(req).contentType(ContentType.JSON).post("rest/feed/modify").then().statusCode(HttpStatus.SC_OK);

		// receive two additional entries, those will trigger two push notifications
		feedNowReturnsMoreEntries();
		forceRefreshAllFeeds();

		// await push notification for the two entries in the feed
		Awaitility.await()
				.atMost(Duration.ofSeconds(20))
				.untilAsserted(() -> getMockServerClient().verify(ntfyPost, VerificationTimes.exactly(2)));
	}

}
