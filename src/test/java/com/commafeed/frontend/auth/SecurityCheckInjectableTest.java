package com.commafeed.frontend.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;
import com.commafeed.backend.service.internal.PostLoginActivities;
import com.commafeed.frontend.auth.SecurityCheckProvider.SecurityCheckInjectable;
import com.commafeed.frontend.session.SessionHelper;
import com.google.common.base.Optional;

public class SecurityCheckInjectableTest {

	@Test
	public void cookie_login_should_perform_post_login_activities_if_user_is_logged_in() {
		User userInSession = new User();

		SessionHelper sessionHelper = mock(SessionHelper.class);
		when(sessionHelper.getLoggedInUser()).thenReturn(Optional.of(userInSession));

		PostLoginActivities postLoginActivities = mock(PostLoginActivities.class);

		UserService service = new UserService(null, null, null, null, null, postLoginActivities);

		SecurityCheckInjectable injectable = new SecurityCheckInjectable(sessionHelper, service, null, false);
		injectable.cookieSessionLogin();

		verify(postLoginActivities).executeFor(userInSession);
	}

}
