package com.commafeed.integration;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.request.FeedModificationRequest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@Slf4j
class WebSocketIT extends BaseIT {

	@BeforeEach
	void setup() {
		RestAssured.authentication = RestAssured.preemptive().basic("admin", "admin");
	}

	@Test
	void sessionClosedIfNotLoggedIn() throws DeploymentException, IOException {
		AtomicBoolean connected = new AtomicBoolean();
		AtomicReference<CloseReason> closeReasonRef = new AtomicReference<>();
		try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(new Endpoint() {
			@Override
			public void onOpen(Session session, EndpointConfig config) {
				connected.set(true);
			}

			@Override
			public void onClose(Session session, CloseReason closeReason) {
				closeReasonRef.set(closeReason);
			}
		}, buildConfig(List.of()), URI.create(getWebSocketUrl()))) {
			Awaitility.await().atMost(15, TimeUnit.SECONDS).untilTrue(connected);
			log.info("connected to {}", session.getRequestURI());

			Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> closeReasonRef.get() != null);
		}
	}

	@Test
	void subscribeAndGetsNotified() throws DeploymentException, IOException {
		List<HttpCookie> cookies = login();

		AtomicBoolean connected = new AtomicBoolean();
		AtomicReference<String> messageRef = new AtomicReference<>();
		try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(new Endpoint() {
			@Override
			public void onOpen(Session session, EndpointConfig config) {
				session.addMessageHandler(String.class, messageRef::set);
				connected.set(true);
			}
		}, buildConfig(cookies), URI.create(getWebSocketUrl()))) {
			Awaitility.await().atMost(15, TimeUnit.SECONDS).untilTrue(connected);
			log.info("connected to {}", session.getRequestURI());

			Long subscriptionId = subscribe(getFeedUrl());

			Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> messageRef.get() != null);
			Assertions.assertEquals("new-feed-entries:" + subscriptionId + ":2", messageRef.get());
		}
	}

	@Test
	void notNotifiedForFilteredEntries() throws DeploymentException, IOException {
		List<HttpCookie> cookies = login();
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

		FeedModificationRequest req = new FeedModificationRequest();
		req.setId(subscriptionId);
		req.setName("feed-name");
		req.setFilter("!title.contains('item 4')");
		RestAssured.given().body(req).contentType(MediaType.APPLICATION_JSON).post("rest/feed/modify").then().statusCode(HttpStatus.SC_OK);

		AtomicBoolean connected = new AtomicBoolean();
		AtomicReference<String> messageRef = new AtomicReference<>();
		try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(new Endpoint() {
			@Override
			public void onOpen(Session session, EndpointConfig config) {
				session.addMessageHandler(String.class, messageRef::set);
				connected.set(true);
			}
		}, buildConfig(cookies), URI.create(getWebSocketUrl()))) {
			Awaitility.await().atMost(15, TimeUnit.SECONDS).untilTrue(connected);
			log.info("connected to {}", session.getRequestURI());

			feedNowReturnsMoreEntries();
			forceRefreshAllFeeds();

			Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> messageRef.get() != null);
			Assertions.assertEquals("new-feed-entries:" + subscriptionId + ":1", messageRef.get());
		}

	}

	@Test
	void pingPong() throws DeploymentException, IOException {
		List<HttpCookie> cookies = login();

		AtomicBoolean connected = new AtomicBoolean();
		AtomicReference<String> messageRef = new AtomicReference<>();
		try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(new Endpoint() {
			@Override
			public void onOpen(Session session, EndpointConfig config) {
				session.addMessageHandler(String.class, messageRef::set);
				connected.set(true);
			}
		}, buildConfig(cookies), URI.create(getWebSocketUrl()))) {
			Awaitility.await().atMost(15, TimeUnit.SECONDS).untilTrue(connected);
			log.info("connected to {}", session.getRequestURI());

			session.getAsyncRemote().sendText("ping");

			Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> messageRef.get() != null);
			Assertions.assertEquals("pong", messageRef.get());
		}
	}

	private ClientEndpointConfig buildConfig(List<HttpCookie> cookies) {
		return ClientEndpointConfig.Builder.create().configurator(new ClientEndpointConfig.Configurator() {
			@Override
			public void beforeRequest(Map<String, List<String>> headers) {
				headers.put(HttpHeaders.COOKIE,
						Collections.singletonList(cookies.stream().map(HttpCookie::toString).collect(Collectors.joining(";"))));
			}
		}).build();
	}

}
