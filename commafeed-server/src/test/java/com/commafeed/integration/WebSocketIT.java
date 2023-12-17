package com.commafeed.integration;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

class WebSocketIT extends BaseIT {

	@Test
	void subscribeAndGetsNotified() throws DeploymentException, IOException {
		String sessionId = login();
		ClientEndpointConfig config = ClientEndpointConfig.Builder.create().configurator(new ClientEndpointConfig.Configurator() {
			@Override
			public void beforeRequest(Map<String, List<String>> headers) {
				headers.put("Cookie", Collections.singletonList("JSESSIONID=" + sessionId));
			}
		}).build();

		AtomicBoolean connected = new AtomicBoolean();
		AtomicReference<String> messageRef = new AtomicReference<>();
		try (Session ignored = ContainerProvider.getWebSocketContainer().connectToServer(new Endpoint() {
			@Override
			public void onOpen(Session session, EndpointConfig config) {
				session.addMessageHandler(new MessageHandler.Whole<String>() {
					@Override
					public void onMessage(String message) {
						messageRef.set(message);
					}
				});
				connected.set(true);
			}
		}, config, URI.create(getWebSocketUrl()))) {
			Awaitility.await().atMost(15, TimeUnit.SECONDS).untilTrue(connected);

			Long subscriptionId = subscribe(getFeedUrl());

			Awaitility.await().atMost(15, TimeUnit.SECONDS).until(() -> messageRef.get() != null);
			Assertions.assertEquals("new-feed-entries:" + subscriptionId, messageRef.get());
		}
	}

}
