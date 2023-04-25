package com.commafeed.frontend.ws;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import com.commafeed.backend.model.User;
import com.commafeed.frontend.session.SessionHelper;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
public class WebSocketConfigurator extends Configurator {

	public static final String SESSIONKEY_USERID = "userId";

	private final WebSocketSessions webSocketSessions;

	@Override
	public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		if (httpSession != null) {
			Optional<User> user = SessionHelper.getLoggedInUser(httpSession);
			if (user.isPresent()) {
				config.getUserProperties().put(SESSIONKEY_USERID, user.get().getId());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
		return (T) new WebSocketEndpoint(webSocketSessions);
	}
}
