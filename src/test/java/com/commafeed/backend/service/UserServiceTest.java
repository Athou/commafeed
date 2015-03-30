package com.commafeed.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.internal.PostLoginActivities;

public class UserServiceTest {

	private static final byte[] SALT = new byte[] { 1, 2, 3 };
	private static final byte[] ENCRYPTED_PASSWORD = new byte[] { 5, 6, 7 };

	@Mock
	private CommaFeedConfiguration commaFeedConfiguration;
	@Mock
	private FeedCategoryDAO feedCategoryDAO;
	@Mock
	private FeedSubscriptionDAO feedSubscriptionDAO;
	@Mock
	private UserDAO userDAO;
	@Mock
	private UserSettingsDAO userSettingsDAO;
	@Mock
	private UserRoleDAO userRoleDAO;
	@Mock
	private PasswordEncryptionService passwordEncryptionService;
	@Mock
	private PostLoginActivities postLoginActivities;

	private User disabledUser;
	private User normalUser;

	private UserService userService;

	@Before
	public void before_each_test() {
		MockitoAnnotations.initMocks(this);

		userService = new UserService(feedCategoryDAO, feedSubscriptionDAO, userDAO, userRoleDAO, userSettingsDAO,
				passwordEncryptionService, commaFeedConfiguration, postLoginActivities);

		disabledUser = new User();
		disabledUser.setDisabled(true);

		normalUser = new User();
		normalUser.setDisabled(false);
		normalUser.setSalt(SALT);
		normalUser.setPassword(ENCRYPTED_PASSWORD);
	}

	@Test
	public void calling_login_should_not_return_user_object_when_given_null_nameOrEmail() {
		Optional<User> user = userService.login(null, "password");
		assertFalse(user.isPresent());
	}

	@Test
	public void calling_login_should_not_return_user_object_when_given_null_password() {
		Optional<User> user = userService.login("testusername", null);
		assertFalse(user.isPresent());
	}

	@Test
	public void calling_login_should_lookup_user_by_name() {
		userService.login("test", "password");
		verify(userDAO).findByName("test");
	}

	@Test
	public void calling_login_should_lookup_user_by_email_if_lookup_by_name_failed() {
		when(userDAO.findByName("test@test.com")).thenReturn(null);
		userService.login("test@test.com", "password");
		verify(userDAO).findByEmail("test@test.com");
	}

	@Test
	public void calling_login_should_not_return_user_object_if_could_not_find_user_by_name_or_email() {
		when(userDAO.findByName("test@test.com")).thenReturn(null);
		when(userDAO.findByEmail("test@test.com")).thenReturn(null);

		Optional<User> user = userService.login("test@test.com", "password");

		assertFalse(user.isPresent());
	}

	@Test
	public void calling_login_should_not_return_user_object_if_user_is_disabled() {
		when(userDAO.findByName("test")).thenReturn(disabledUser);
		Optional<User> user = userService.login("test", "password");
		assertFalse(user.isPresent());
	}

	@Test
	public void calling_login_should_try_to_authenticate_user_who_is_not_disabled() {
		when(userDAO.findByName("test")).thenReturn(normalUser);
		when(passwordEncryptionService.authenticate(anyString(), any(byte[].class), any(byte[].class))).thenReturn(false);

		userService.login("test", "password");

		verify(passwordEncryptionService).authenticate("password", ENCRYPTED_PASSWORD, SALT);
	}

	@Test
	public void calling_login_should_not_return_user_object_on_unsuccessful_authentication() {
		when(userDAO.findByName("test")).thenReturn(normalUser);
		when(passwordEncryptionService.authenticate(anyString(), any(byte[].class), any(byte[].class))).thenReturn(false);

		Optional<User> authenticatedUser = userService.login("test", "password");

		assertFalse(authenticatedUser.isPresent());
	}

	@Test
	public void calling_login_should_execute_post_login_activities_for_user_on_successful_authentication() {
		when(userDAO.findByName("test")).thenReturn(normalUser);
		when(passwordEncryptionService.authenticate(anyString(), any(byte[].class), any(byte[].class))).thenReturn(true);
		doNothing().when(postLoginActivities).executeFor(any(User.class));

		userService.login("test", "password");

		verify(postLoginActivities).executeFor(normalUser);
	}

	@Test
	public void calling_login_should_return_user_object_on_successful_authentication() {
		when(userDAO.findByName("test")).thenReturn(normalUser);
		when(passwordEncryptionService.authenticate(anyString(), any(byte[].class), any(byte[].class))).thenReturn(true);
		doNothing().when(postLoginActivities).executeFor(any(User.class));

		Optional<User> authenticatedUser = userService.login("test", "password");

		assertTrue(authenticatedUser.isPresent());
		assertEquals(normalUser, authenticatedUser.get());
	}

	@Test
	public void api_login_should_not_return_user_if_apikey_null() {
		Optional<User> user = userService.login(null);
		assertFalse(user.isPresent());
	}

	@Test
	public void api_login_should_lookup_user_by_apikey() {
		when(userDAO.findByApiKey("apikey")).thenReturn(null);
		userService.login("apikey");
		verify(userDAO).findByApiKey("apikey");
	}

	@Test
	public void api_login_should_not_return_user_if_user_not_found_from_lookup_by_apikey() {
		when(userDAO.findByApiKey("apikey")).thenReturn(null);
		Optional<User> user = userService.login("apikey");
		assertFalse(user.isPresent());
	}

	@Test
	public void api_login_should_not_return_user_if_user_found_from_apikey_lookup_is_disabled() {
		when(userDAO.findByApiKey("apikey")).thenReturn(disabledUser);
		Optional<User> user = userService.login("apikey");
		assertFalse(user.isPresent());
	}

	@Test
	public void api_login_should_perform_post_login_activities_if_user_found_from_apikey_lookup_not_disabled() {
		when(userDAO.findByApiKey("apikey")).thenReturn(normalUser);
		userService.login("apikey");
		verify(postLoginActivities).executeFor(normalUser);
	}

	@Test
	public void api_login_should_return_user_if_user_found_from_apikey_lookup_not_disabled() {
		when(userDAO.findByApiKey("apikey")).thenReturn(normalUser);
		Optional<User> returnedUser = userService.login("apikey");
		assertEquals(normalUser, returnedUser.get());
	}

}
