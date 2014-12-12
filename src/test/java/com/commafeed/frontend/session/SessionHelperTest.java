package com.commafeed.frontend.session;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Test;

import com.commafeed.backend.model.User;

public class SessionHelperTest {

	private static String SESSION_KEY_USER = "user";

	@Test
	public void getting_user_does_not_create_a_session_if_not_present() {
		HttpServletRequest request = mock(HttpServletRequest.class);

		SessionHelper sessionHelper = new SessionHelper(request);
		sessionHelper.getLoggedInUser();

		verify(request).getSession(false);
	}

	@Test
	public void getting_user_should_not_return_user_if_there_is_no_preexisting_http_session() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(false)).thenReturn(null);

		SessionHelper sessionHelper = new SessionHelper(request);
		Optional<User> user = sessionHelper.getLoggedInUser();

		Assert.assertFalse(user.isPresent());
	}

	@Test
	public void getting_user_should_not_return_user_if_user_not_present_in_http_session() {
		HttpSession session = mock(HttpSession.class);
		when(session.getAttribute(SESSION_KEY_USER)).thenReturn(null);

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(false)).thenReturn(session);

		SessionHelper sessionHelper = new SessionHelper(request);
		Optional<User> user = sessionHelper.getLoggedInUser();

		Assert.assertFalse(user.isPresent());
	}

	@Test
	public void getting_user_should_return_user_if_user_present_in_http_session() {
		User userInSession = new User();

		HttpSession session = mock(HttpSession.class);
		when(session.getAttribute(SESSION_KEY_USER)).thenReturn(userInSession);

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(false)).thenReturn(session);

		SessionHelper sessionHelper = new SessionHelper(request);
		Optional<User> user = sessionHelper.getLoggedInUser();

		Assert.assertTrue(user.isPresent());
		Assert.assertEquals(userInSession, user.get());
	}

}
