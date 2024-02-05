package com.commafeed.frontend.ws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.backend.model.User;

import jakarta.websocket.Session;

@ExtendWith(MockitoExtension.class)
class WebSocketSessionsTest {

	@Mock
	private MetricRegistry metrics;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Session session1;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Session session2;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Session session3;

	private WebSocketSessions webSocketSessions;

	@BeforeEach
	void init() {
		webSocketSessions = new WebSocketSessions(metrics);
	}

	@Test
	void sendsMessageToUser() {
		Mockito.when(session1.isOpen()).thenReturn(true);
		Mockito.when(session2.isOpen()).thenReturn(true);

		User user1 = newUser(1L);
		webSocketSessions.add(user1.getId(), session1);
		webSocketSessions.add(user1.getId(), session2);

		User user2 = newUser(2L);
		webSocketSessions.add(user2.getId(), session3);

		webSocketSessions.sendMessage(user1, "Hello");
		Mockito.verify(session1).getAsyncRemote();
		Mockito.verify(session2).getAsyncRemote();
		Mockito.verifyNoInteractions(session3);
	}

	@Test
	void closedSessionsAreNotNotified() {
		Mockito.when(session1.isOpen()).thenReturn(false);

		User user1 = newUser(1L);
		webSocketSessions.add(user1.getId(), session1);

		webSocketSessions.sendMessage(user1, "Hello");
		Mockito.verify(session1, Mockito.never()).getAsyncRemote();
	}

	@Test
	void removedSessionsAreNotNotified() {
		User user1 = newUser(1L);
		webSocketSessions.add(user1.getId(), session1);
		webSocketSessions.remove(session1);

		webSocketSessions.sendMessage(user1, "Hello");
		Mockito.verifyNoInteractions(session1);
	}

	private User newUser(Long userId) {
		User user = new User();
		user.setId(userId);
		return user;
	}
}