package com.commafeed.frontend.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;
import com.commafeed.backend.service.internal.PostLoginActivities;
import com.commafeed.frontend.session.SessionHelper;

public class SecurityCheckFactoryTest {

	@Test
	public void cookie_login_should_perform_post_login_activities_if_user_is_logged_in() {
		User userInSession = new User();

		SessionHelper sessionHelper = mock(SessionHelper.class);
		when(sessionHelper.getLoggedInUser()).thenReturn(Optional.of(userInSession));

		PostLoginActivities postLoginActivities = mock(PostLoginActivities.class);

		UserService service = new UserService(null, null, null, null, null, null, null, postLoginActivities);

		SecurityCheckFactory factory = new SecurityCheckFactory(null, false);
		factory.userService = service;
		factory.cookieSessionLogin(sessionHelper);

		verify(postLoginActivities).executeFor(userInSession);
	}

}
