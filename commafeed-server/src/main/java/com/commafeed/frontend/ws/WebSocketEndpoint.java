package com.commafeed.frontend.ws;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

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
		} else {
			log.debug("created websocket session for user {}", userId);
			sessions.add(userId, session);
		}

		// converting this anonymous inner class to a lambda causes the following error when a message is sent from the client
		// Unable to find decoder for type <javax.websocket.MessageHandler$Whole>
		// this error is only visible when registering a listener to ws.onclose on the client
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(String message) {
				if ("ping".equals(message)) {
					session.getAsyncRemote().sendText("pong");
				}
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
