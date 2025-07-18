package com.commafeed.frontend.ws;

import java.io.IOException;

import jakarta.inject.Singleton;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.model.User;
import com.commafeed.security.AuthenticationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@ServerEndpoint("/ws")
@RequiredArgsConstructor
public class WebSocketEndpoint {

	private final AuthenticationContext authenticationContext;
	private final CommaFeedConfiguration config;
	private final WebSocketSessions sessions;

	@OnOpen
	public void onOpen(Session session) throws IOException {
		User user = authenticationContext.getCurrentUser();
		if (user == null) {
			reject(session);
			return;
		}

		log.debug("created websocket session for user '{}'", user.getName());
		sessions.add(user.getId(), session);
		session.setMaxIdleTimeout(config.websocket().pingInterval().toMillis() + 10000);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		if ("ping".equals(message)) {
			session.getAsyncRemote().sendText("pong");
		}
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
	}

	private void reject(Session session) throws IOException {
		session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "unauthorized"));
	}

}
