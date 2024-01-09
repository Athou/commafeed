package com.commafeed.frontend.session;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.commafeed.backend.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

class SessionHelperTest {

	@Test
	void gettingUserDoesNotCreateSession() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

		SessionHelper sessionHelper = new SessionHelper(request);
		sessionHelper.getLoggedInUserId();

		Mockito.verify(request).getSession(false);
	}

	@Test
	void gettingUserShouldNotReturnUserIfThereIsNoPreexistingHttpSession() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getSession(false)).thenReturn(null);

		SessionHelper sessionHelper = new SessionHelper(request);
		Optional<Long> userId = sessionHelper.getLoggedInUserId();

		Assertions.assertFalse(userId.isPresent());
	}

	@Test
	void gettingUserShouldNotReturnUserIfUserNotPresentInHttpSession() {
		HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(SessionHelper.SESSION_KEY_USER_ID)).thenReturn(null);

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getSession(false)).thenReturn(session);

		SessionHelper sessionHelper = new SessionHelper(request);
		Optional<Long> userId = sessionHelper.getLoggedInUserId();

		Assertions.assertFalse(userId.isPresent());
	}

	@Test
	void gettingUserShouldReturnUserIfUserPresentInHttpSession() {
		User userInSession = new User();

		HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(SessionHelper.SESSION_KEY_USER_ID)).thenReturn(1L);

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getSession(false)).thenReturn(session);

		SessionHelper sessionHelper = new SessionHelper(request);
		Optional<Long> userId = sessionHelper.getLoggedInUserId();

		Assertions.assertTrue(userId.isPresent());
	}

}
