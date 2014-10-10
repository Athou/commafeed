package com.commafeed.frontend.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.model.request.LoginRequest;
import com.commafeed.frontend.model.request.RegistrationRequest;
import com.google.common.base.Optional;

public class UserRestTest {
	
	@Test public void
	login_should_not_populate_http_session_if_unsuccessfull() {
		// Absent user
		Optional<User> absentUser = Optional.absent();
		
		// Create UserService partial mock
		UserService service = mock(UserService.class);
		when(service.login("user", "password")).thenReturn(absentUser);
		
		HttpSession session = mock(HttpSession.class);
		UserREST userREST = new UserREST(null, null, null, service, null, null, null);
		
		LoginRequest req = new LoginRequest();
		req.setName("user");
		req.setPassword("password");
		
		userREST.login(req, session);
		
		verifyZeroInteractions(session);
	}
	
	@Test public void
	login_should_populate_http_session_if_successfull() {
		// Create a user
		User user = new User();
		
		// Create UserService mock
		UserService service = mock(UserService.class);
		when(service.login("user", "password")).thenReturn(Optional.of(user));
		
		HttpSession session = mock(HttpSession.class);
		UserREST userREST = new UserREST(null, null, null, service, null, null, null);
		
		LoginRequest req = new LoginRequest();
		req.setName("user");
		req.setPassword("password");
		
		userREST.login(req, session);
		
		verify(session).setAttribute(UserREST.SESSION_KEY_USER, user);
	}
	
	@Test public void
	register_should_register_and_then_login() {
		// Create UserService mock
		UserService service = mock(UserService.class);
		
		RegistrationRequest req = new RegistrationRequest();
		req.setName("user");
		req.setPassword("password");
		req.setEmail("test@test.com");
		
		HttpSession session = mock(HttpSession.class);
		
		InOrder inOrder = inOrder(service);
		
		UserREST userREST = new UserREST(null, null, null, service, null, null, null);
		userREST.register(req, session);
		
		inOrder.verify(service).register("user", "password", "test@test.com", Arrays.asList(Role.USER));
		inOrder.verify(service).login("user", "password");
	}
	
	@Test public void
	register_should_populate_http_session() {
		// Create a user
		User user = new User();
		
		// Create UserService partial mock
		UserService service = mock(UserService.class);
		when(service.register(any(String.class), any(String.class), any(String.class), Matchers.anyListOf(Role.class))).thenReturn(user);
		when(service.login(any(String.class), any(String.class))).thenReturn(Optional.of(user));
		
		RegistrationRequest req = new RegistrationRequest();
		req.setName("user");
		req.setPassword("password");
		req.setEmail("test@test.com");
		
		HttpSession session = mock(HttpSession.class);
		
		UserREST userREST = new UserREST(null, null, null, service, null, null, null);
		userREST.register(req, session);
		
		verify(session).setAttribute(UserREST.SESSION_KEY_USER, user);
	}

}
