package com.commafeed.frontend.ws;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
public class WebSocketEndpoint extends Endpoint {

	private final WebSocketSessions sessions;

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		Long userId = (Long) config.getUserProperties().get(WebSocketConfigurator.SESSIONKEY_USERID);
		if (userId == null) {
			reject(session);
			return;
		}

		log.debug("created websocket session for user {}", userId);
		sessions.add(userId, session);

		session.addMessageHandler(String.class, message -> {
			if ("ping".equals(message)) {
				session.getAsyncRemote().sendText("pong");
			}
		});
	}

	private void reject(Session session) {
		try {
			session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "unauthorized"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onClose(Session session, CloseReason reason) {
		sessions.remove(session);
	}

}
