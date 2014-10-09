package com.commafeed.frontend.resource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpSession;

import org.junit.Test;

import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.model.request.LoginRequest;
import com.google.common.base.Optional;

public class UserRestTest {
	
	@Test public void
	login_should_not_populate_http_session_if_unsuccessfull() {
		// Absent user
		Optional<User> absentUser = Optional.absent();
		
		// Create UserService partial mock
		UserService service = mock(UserService.class);
		when(service.login("user", "password")).thenReturn(absentUser);
		when(service.login(any(String.class), any(String.class), any(HttpSession.class))).thenCallRealMethod();
		
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
		
		// Create UserService partial mock
		UserService service = mock(UserService.class);
		when(service.login("user", "password")).thenReturn(Optional.of(user));
		when(service.login(any(String.class), any(String.class), any(HttpSession.class))).thenCallRealMethod();
		
		HttpSession session = mock(HttpSession.class);
		UserREST userREST = new UserREST(null, null, null, service, null, null, null);
		
		LoginRequest req = new LoginRequest();
		req.setName("user");
		req.setPassword("password");
		
		userREST.login(req, session);
		
		verify(session).setAttribute("user", user);
	}

}
