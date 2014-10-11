package com.commafeed.frontend.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Test;

import com.commafeed.backend.model.User;
import com.commafeed.backend.service.UserService;
import com.commafeed.backend.service.internal.PostLoginActivities;
import com.commafeed.frontend.auth.SecurityCheckProvider.SecurityCheckInjectable;
import com.commafeed.frontend.resource.UserREST;
import com.google.common.base.Optional;

public class SecurityCheckInjectableTest {
	
	@Test public void
	cookie_login_does_not_create_a_session_if_not_present() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		UserService service = mock(UserService.class);
		
		SecurityCheckInjectable injectable = new SecurityCheckInjectable(request, service, null, false);
		injectable.cookieSessionLogin();
		
		verify(request).getSession(false);
	}
	
	@Test public void
	cookie_login_should_not_return_user_if_there_is_no_preexisting_http_session() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(false)).thenReturn(null);
		
		UserService service = new UserService(null, null, null, null, null, null);
		
		SecurityCheckInjectable injectable = new SecurityCheckInjectable(request, service, null, false);
		Optional<User> user = injectable.cookieSessionLogin();
		
		Assert.assertFalse(user.isPresent());
	}
	
	@Test public void
	cookie_login_should_not_return_user_if_user_not_present_in_http_session() {
		HttpSession session = mock(HttpSession.class);
		when(session.getAttribute(UserREST.SESSION_KEY_USER)).thenReturn(null);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(false)).thenReturn(session);
		
		UserService service = new UserService(null, null, null, null, null, null);
		
		SecurityCheckInjectable injectable = new SecurityCheckInjectable(request, service, null, false);
		Optional<User> user = injectable.cookieSessionLogin();
		
		Assert.assertFalse(user.isPresent());
	}
	
	@Test public void
	cookie_login_should_perform_post_login_activities_if_user_present_in_http_session() {
		User userInSession = new User();
		
		HttpSession session = mock(HttpSession.class);
		when(session.getAttribute(UserREST.SESSION_KEY_USER)).thenReturn(userInSession);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(false)).thenReturn(session);
		
		PostLoginActivities postLoginActivities = mock(PostLoginActivities.class);
		
		UserService service = new UserService(null, null, null, null, null, postLoginActivities);
		
		SecurityCheckInjectable injectable = new SecurityCheckInjectable(request, service, null, false);
		Optional<User> user = injectable.cookieSessionLogin();
		
		verify(postLoginActivities).executeFor(userInSession);
	}
	
	@Test public void
	calling_login_should_return_user_if_user_present_in_http_session() {
		User userInSession = new User();
		
		HttpSession session = mock(HttpSession.class);
		when(session.getAttribute(UserREST.SESSION_KEY_USER)).thenReturn(userInSession);
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession(false)).thenReturn(session);
		
		PostLoginActivities postLoginActivities = mock(PostLoginActivities.class);
		
		UserService service = new UserService(null, null, null, null, null, postLoginActivities);
		
		SecurityCheckInjectable injectable = new SecurityCheckInjectable(request, service, null, false);
		Optional<User> user = injectable.cookieSessionLogin();
		
		Assert.assertTrue(user.isPresent());
		Assert.assertEquals(userInSession, user.get());
	}

}
