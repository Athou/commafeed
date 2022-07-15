package com.commafeed.frontend.session;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.commafeed.backend.model.User;

class SessionHelperTest {

	private static final String SESSION_KEY_USER = "user";

	@Test
	void gettingUserDoesNotCreateSession() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

		SessionHelper sessionHelper = new SessionHelper(request);
		sessionHelper.getLoggedInUser();

		Mockito.verify(request).getSession(false);
	}

	@Test
	void gettingUserShouldNotReturnUserIfThereIsNoPreexistingHttpSession() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getSession(false)).thenReturn(null);

		SessionHelper sessionHelper = new SessionHelper(request);
		Optional<User> user = sessionHelper.getLoggedInUser();

		Assertions.assertFalse(user.isPresent());
	}

	@Test
	void gettingUserShouldNotReturnUserIfUserNotPresentInHttpSession() {
		HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(SESSION_KEY_USER)).thenReturn(null);

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getSession(false)).thenReturn(session);

		SessionHelper sessionHelper = new SessionHelper(request);
		Optional<User> user = sessionHelper.getLoggedInUser();

		Assertions.assertFalse(user.isPresent());
	}

	@Test
	void gettingUserShouldReturnUserIfUserPresentInHttpSession() {
		User userInSession = new User();

		HttpSession session = Mockito.mock(HttpSession.class);
		Mockito.when(session.getAttribute(SESSION_KEY_USER)).thenReturn(userInSession);

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getSession(false)).thenReturn(session);

		SessionHelper sessionHelper = new SessionHelper(request);
		Optional<User> user = sessionHelper.getLoggedInUser();

		Assertions.assertTrue(user.isPresent());
		Assertions.assertEquals(userInSession, user.get());
	}

}
