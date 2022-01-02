package com.commafeed.frontend.resource;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.model.request.LoginRequest;
import com.commafeed.frontend.model.request.RegistrationRequest;
import com.commafeed.frontend.session.SessionHelper;

public class UserRestTest {

	@Test
	public void loginShouldNotPopulateHttpSessionIfUnsuccessfull() {
		// Absent user
		Optional<User> absentUser = Optional.empty();

		// Create UserService partial mock
		UserService service = Mockito.mock(UserService.class);
		Mockito.when(service.login("user", "password")).thenReturn(absentUser);

		UserREST userREST = new UserREST(null, null, null, service, null, null, null);
		SessionHelper sessionHelper = Mockito.mock(SessionHelper.class);

		LoginRequest req = new LoginRequest();
		req.setName("user");
		req.setPassword("password");

		userREST.login(req, sessionHelper);

		Mockito.verify(sessionHelper, Mockito.never()).setLoggedInUser(Mockito.any(User.class));
	}

	@Test
	public void loginShouldPopulateHttpSessionIfSuccessfull() {
		// Create a user
		User user = new User();

		// Create UserService mock
		UserService service = Mockito.mock(UserService.class);
		Mockito.when(service.login("user", "password")).thenReturn(Optional.of(user));

		LoginRequest req = new LoginRequest();
		req.setName("user");
		req.setPassword("password");

		UserREST userREST = new UserREST(null, null, null, service, null, null, null);
		SessionHelper sessionHelper = Mockito.mock(SessionHelper.class);

		userREST.login(req, sessionHelper);

		Mockito.verify(sessionHelper).setLoggedInUser(user);
	}

	@Test
	public void registerShouldRegisterAndThenLogin() {
		// Create UserService mock
		UserService service = Mockito.mock(UserService.class);

		RegistrationRequest req = new RegistrationRequest();
		req.setName("user");
		req.setPassword("password");
		req.setEmail("test@test.com");

		InOrder inOrder = Mockito.inOrder(service);

		SessionHelper sessionHelper = Mockito.mock(SessionHelper.class);
		UserREST userREST = new UserREST(null, null, null, service, null, null, null);

		userREST.registerUser(req, sessionHelper);

		inOrder.verify(service).register("user", "password", "test@test.com", Arrays.asList(Role.USER));
		inOrder.verify(service).login("user", "password");
	}

	@Test
	public void registerShouldPopulateHttpSession() {
		// Create a user
		User user = new User();

		// Create UserService mock
		UserService service = Mockito.mock(UserService.class);
		Mockito.when(service.register(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class),
				ArgumentMatchers.anyList())).thenReturn(user);
		Mockito.when(service.login(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(Optional.of(user));

		RegistrationRequest req = new RegistrationRequest();
		req.setName("user");
		req.setPassword("password");
		req.setEmail("test@test.com");

		SessionHelper sessionHelper = Mockito.mock(SessionHelper.class);
		UserREST userREST = new UserREST(null, null, null, service, null, null, null);

		userREST.registerUser(req, sessionHelper);

		Mockito.verify(sessionHelper).setLoggedInUser(user);
	}

}
