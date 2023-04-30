package com.commafeed.frontend.ws;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.websocket.Session;

import com.commafeed.backend.model.User;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class WebSocketSessions {

	// a user may have multiple sessions (two tabs, on mobile, ...)
	private final Map<Long, Set<Session>> sessions = new ConcurrentHashMap<>();

	public void add(Long userId, Session session) {
		sessions.computeIfAbsent(userId, v -> ConcurrentHashMap.newKeySet()).add(session);
	}

	public void remove(Session session) {
		sessions.values().forEach(v -> v.removeIf(e -> e.equals(session)));
	}

	public void sendMessage(User user, String text) {
		Set<Session> userSessions = sessions.entrySet()
				.stream()
				.filter(e -> e.getKey().equals(user.getId()))
				.flatMap(e -> e.getValue().stream())
				.collect(Collectors.toSet());

		if (!userSessions.isEmpty()) {
			log.debug("sending '{}' to {} users via websocket", text, userSessions.size());
			for (Session userSession : userSessions) {
				if (userSession.isOpen()) {
					userSession.getAsyncRemote().sendText(text);
				}
			}
		}
	}
}
