package com.commafeed.frontend.ws;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.backend.model.User;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class WebSocketSessions {

	// a user may have multiple sessions (two tabs, two devices, ...)
	private final Map<Long, Set<Session>> sessions = new ConcurrentHashMap<>();

	@Inject
	public WebSocketSessions(MetricRegistry metrics) {
		metrics.register(MetricRegistry.name(getClass(), "users"),
				(Gauge<Long>) () -> sessions.values().stream().filter(v -> !v.isEmpty()).count());
		metrics.register(MetricRegistry.name(getClass(), "sessions"),
				(Gauge<Long>) () -> sessions.values().stream().mapToLong(Set::size).sum());
	}

	public void add(Long userId, Session session) {
		sessions.computeIfAbsent(userId, v -> ConcurrentHashMap.newKeySet()).add(session);
	}

	public void remove(Session session) {
		sessions.values().forEach(v -> v.remove(session));
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
