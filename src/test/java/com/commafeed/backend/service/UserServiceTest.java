package com.commafeed.backend.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.internal.PostLoginActivities;
import com.google.common.base.Optional;

public class UserServiceTest {
	
	@Test public void 
	calling_login_should_not_return_user_object_when_given_null_nameOrEmail() {
		UserService service = new UserService(null, null, null, null, null, null);
		
		Optional<User> user = service.login(null, "password");
		
		Assert.assertFalse(user.isPresent());
	}
	
	@Test public void
	calling_login_should_not_return_user_object_when_given_null_password() {
		UserService service = new UserService(null, null, null, null, null, null);
		
		Optional<User> user = service.login("testusername", null);
		
		Assert.assertFalse(user.isPresent());
	}
	
	@Test public void
	calling_login_should_lookup_user_by_name() {
		UserDAO dao = mock(UserDAO.class);
		
		UserService service = new UserService(null, dao, null, null, null, null);
		service.login("test", "password");
		
		verify(dao).findByName("test");
	}
	
	@Test public void
	calling_login_should_lookup_user_by_email_if_lookup_by_name_failed() {
		UserDAO dao = mock(UserDAO.class);
		when(dao.findByName("test@test.com")).thenReturn(null);
		
		UserService service = new UserService(null, dao, null, null, null, null);
		service.login("test@test.com", "password");
		
		verify(dao).findByEmail("test@test.com");
	}
	
	@Test public void
	calling_login_should_not_return_user_object_if_user_is_disabled() {
		// Make a disabled user
		User disabledUser = new User();
		disabledUser.setDisabled(true);
		
		// Mock DAO to return the disabled user
		UserDAO dao = mock(UserDAO.class);
		when(dao.findByName("test")).thenReturn(disabledUser);
		
		// Create service with mocked DAO
		UserService service = new UserService(null, dao, null, null, null, null);
		
		// Try to login as the disabled user
		Optional<User> user = service.login("test", "password");
		
		Assert.assertFalse(user.isPresent());
	}
	
	@Test public void
	calling_login_should_try_to_authenticate_user_who_is_not_disabled() {
		// Make a user who is not disabled
		User user = new User();
		user.setDisabled(false);
		
		// Set the encryptedPassword on the user
		byte[] encryptedPassword = new byte[]{5,6,7};
		user.setPassword(encryptedPassword);
		
		// Set a salt for this user
		byte[] salt = new byte[]{1,2,3};
		user.setSalt(salt);
		
		// Mock DAO to return the user
		UserDAO dao = mock(UserDAO.class);
		when(dao.findByName("test")).thenReturn(user);
		
		// Mock PasswordEncryptionService
		PasswordEncryptionService encryptionService = mock(PasswordEncryptionService.class);
		when(encryptionService.authenticate(anyString(), any(byte[].class), any(byte[].class))).thenReturn(false);
		
		// Create service with mocks
		UserService service = new UserService(null, dao, null, encryptionService, null, null);
		
		// Try to login as the user
		service.login("test", "password");
		
		verify(encryptionService).authenticate("password", encryptedPassword, salt);
	}
	
	@Test public void
	calling_login_should_not_return_user_object_on_unsuccessful_authentication() {
		// Make a user who is not disabled
		User user = new User();
		user.setDisabled(false);
		
		// Set the encryptedPassword on the user
		byte[] encryptedPassword = new byte[]{1,2,3};
		user.setPassword(encryptedPassword);
		
		// Set a salt for this user
		byte[] salt = new byte[]{4,5,6};
		user.setSalt(salt);
		
		// Mock DAO to return the user
		UserDAO dao = mock(UserDAO.class);
		when(dao.findByName("test")).thenReturn(user);
		
		// Mock PasswordEncryptionService
		PasswordEncryptionService encryptionService = mock(PasswordEncryptionService.class);
		when(encryptionService.authenticate(anyString(), any(byte[].class), any(byte[].class))).thenReturn(false);
		
		// Create service with mocks
		UserService service = new UserService(null, dao, null, encryptionService, null, null);
		
		// Try to login as the user
		Optional<User> authenticatedUser = service.login("test", "password");
		
		Assert.assertFalse(authenticatedUser.isPresent());
	}
	
	@Test public void
	calling_login_should_return_user_object_on_successful_authentication() {
		// Make a user who is not disabled
		User user = new User();
		user.setDisabled(false);
		
		// Set the encryptedPassword on the user
		byte[] encryptedPassword = new byte[]{1,2,3};
		user.setPassword(encryptedPassword);
		
		// Set a salt for this user
		byte[] salt = new byte[]{4,5,6};
		user.setSalt(salt);
		
		// Mock DAO to return the user
		UserDAO dao = mock(UserDAO.class);
		when(dao.findByName("test")).thenReturn(user);
		
		// Mock PasswordEncryptionService
		PasswordEncryptionService encryptionService = mock(PasswordEncryptionService.class);
		when(encryptionService.authenticate(anyString(), any(byte[].class), any(byte[].class))).thenReturn(true);
		
		// Mock PostLoginActivities to do nothing
		PostLoginActivities postLoginActivities = mock(PostLoginActivities.class);
		doNothing().when(postLoginActivities).executeFor(any(User.class));
		
		// Create service with mocks
		UserService service = new UserService(null, dao, null, encryptionService, null, postLoginActivities);
		
		// Try to login as the user
		Optional<User> authenticatedUser = service.login("test", "password");
		
		Assert.assertTrue(authenticatedUser.isPresent());
		Assert.assertEquals(user, authenticatedUser.get());
	}

}
