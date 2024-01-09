package com.commafeed.frontend.auth;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;
import com.commafeed.backend.service.internal.PostLoginActivities;
import com.commafeed.frontend.session.SessionHelper;

class SecurityCheckFactoryTest {

	@Test
	void cookieLoginShouldPerformPostLoginActivities() {
		User userInSession = new User();
		UserDAO userDAO = Mockito.mock(UserDAO.class);
		Mockito.when(userDAO.findById(1L)).thenReturn(userInSession);

		SessionHelper sessionHelper = Mockito.mock(SessionHelper.class);
		Mockito.when(sessionHelper.getLoggedInUserId()).thenReturn(Optional.of(1L));

		PostLoginActivities postLoginActivities = Mockito.mock(PostLoginActivities.class);

		UserService service = new UserService(null, null, null, null, null, null, null, postLoginActivities);

		SecurityCheckFactory factory = new SecurityCheckFactory(userDAO, service, null, null, false);
		factory.cookieSessionLogin(sessionHelper);

		Mockito.verify(postLoginActivities).executeFor(userInSession);
	}

}
