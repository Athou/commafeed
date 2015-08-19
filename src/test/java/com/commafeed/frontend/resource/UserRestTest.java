package com.commafeed.frontend.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.model.request.LoginRequest;
import com.commafeed.frontend.model.request.RegistrationRequest;
import com.commafeed.frontend.session.SessionHelper;

public class UserRestTest {

	@Test
	public void login_should_not_populate_http_session_if_unsuccessfull() {
		// Absent user
		Optional<User> absentUser = Optional.empty();

		// Create UserService partial mock
		UserService service = mock(UserService.class);
		when(service.login("user", "password")).thenReturn(absentUser);

		UserREST userREST = new UserREST(null, null, null, service, null, null, null);
		SessionHelper sessionHelper = mock(SessionHelper.class);

		LoginRequest req = new LoginRequest();
		req.setName("user");
		req.setPassword("password");

		userREST.login(req, sessionHelper);

		verify(sessionHelper, never()).setLoggedInUser(any(User.class));
	}

	@Test
	public void login_should_populate_http_session_if_successfull() {
		// Create a user
		User user = new User();

		// Create UserService mock
		UserService service = mock(UserService.class);
		when(service.login("user", "password")).thenReturn(Optional.of(user));

		LoginRequest req = new LoginRequest();
		req.setName("user");
		req.setPassword("password");

		UserREST userREST = new UserREST(null, null, null, service, null, null, null);
		SessionHelper sessionHelper = mock(SessionHelper.class);

		userREST.login(req, sessionHelper);

		verify(sessionHelper).setLoggedInUser(user);
	}

	@Test
	public void register_should_register_and_then_login() {
		// Create UserService mock
		UserService service = mock(UserService.class);

		RegistrationRequest req = new RegistrationRequest();
		req.setName("user");
		req.setPassword("password");
		req.setEmail("test@test.com");

		InOrder inOrder = inOrder(service);

		SessionHelper sessionHelper = mock(SessionHelper.class);
		UserREST userREST = new UserREST(null, null, null, service, null, null, null);

		userREST.register(req, sessionHelper);

		inOrder.verify(service).register("user", "password", "test@test.com", Arrays.asList(Role.USER));
		inOrder.verify(service).login("user", "password");
	}

	@Test
	public void register_should_populate_http_session() {
		// Create a user
		User user = new User();

		// Create UserService mock
		UserService service = mock(UserService.class);
		when(service.register(any(String.class), any(String.class), any(String.class), Matchers.anyListOf(Role.class))).thenReturn(user);
		when(service.login(any(String.class), any(String.class))).thenReturn(Optional.of(user));

		RegistrationRequest req = new RegistrationRequest();
		req.setName("user");
		req.setPassword("password");
		req.setEmail("test@test.com");

		SessionHelper sessionHelper = mock(SessionHelper.class);
		UserREST userREST = new UserREST(null, null, null, service, null, null, null);

		userREST.register(req, sessionHelper);

		verify(sessionHelper).setLoggedInUser(user);
	}

}
