package com.commafeed.backend.service;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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
	public void init() {
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
	public void callingLoginShouldNotReturnUserObjectWhenGivenNullNameOrEmail() {
		Optional<User> user = userService.login(null, "password");
		Assert.assertFalse(user.isPresent());
	}

	@Test
	public void callingLoginShouldNotReturnUserObjectWhenGivenNullPassword() {
		Optional<User> user = userService.login("testusername", null);
		Assert.assertFalse(user.isPresent());
	}

	@Test
	public void callingLoginShouldLookupUserByName() {
		userService.login("test", "password");
		Mockito.verify(userDAO).findByName("test");
	}

	@Test
	public void callingLoginShouldLookupUserByEmailIfLookupByNameFailed() {
		Mockito.when(userDAO.findByName("test@test.com")).thenReturn(null);
		userService.login("test@test.com", "password");
		Mockito.verify(userDAO).findByEmail("test@test.com");
	}

	@Test
	public void callingLoginShouldNotReturnUserObjectIfCouldNotFindUserByNameOrEmail() {
		Mockito.when(userDAO.findByName("test@test.com")).thenReturn(null);
		Mockito.when(userDAO.findByEmail("test@test.com")).thenReturn(null);

		Optional<User> user = userService.login("test@test.com", "password");

		Assert.assertFalse(user.isPresent());
	}

	@Test
	public void callingLoginShouldNotReturnUserObjectIfUserIsDisabled() {
		Mockito.when(userDAO.findByName("test")).thenReturn(disabledUser);
		Optional<User> user = userService.login("test", "password");
		Assert.assertFalse(user.isPresent());
	}

	@Test
	public void callingLoginShouldTryToAuthenticateUserWhoIsNotDisabled() {
		Mockito.when(userDAO.findByName("test")).thenReturn(normalUser);
		Mockito.when(passwordEncryptionService.authenticate(Mockito.anyString(), Mockito.any(byte[].class), Mockito.any(byte[].class)))
				.thenReturn(false);

		userService.login("test", "password");

		Mockito.verify(passwordEncryptionService).authenticate("password", ENCRYPTED_PASSWORD, SALT);
	}

	@Test
	public void callingLoginShouldNotReturnUserObjectOnUnsuccessfulAuthentication() {
		Mockito.when(userDAO.findByName("test")).thenReturn(normalUser);
		Mockito.when(passwordEncryptionService.authenticate(Mockito.anyString(), Mockito.any(byte[].class), Mockito.any(byte[].class)))
				.thenReturn(false);

		Optional<User> authenticatedUser = userService.login("test", "password");

		Assert.assertFalse(authenticatedUser.isPresent());
	}

	@Test
	public void callingLoginShouldExecutePostLoginActivitiesForUserOnSuccessfulAuthentication() {
		Mockito.when(userDAO.findByName("test")).thenReturn(normalUser);
		Mockito.when(passwordEncryptionService.authenticate(Mockito.anyString(), Mockito.any(byte[].class), Mockito.any(byte[].class)))
				.thenReturn(true);
		Mockito.doNothing().when(postLoginActivities).executeFor(Mockito.any(User.class));

		userService.login("test", "password");

		Mockito.verify(postLoginActivities).executeFor(normalUser);
	}

	@Test
	public void callingLoginShouldReturnUserObjectOnSuccessfulAuthentication() {
		Mockito.when(userDAO.findByName("test")).thenReturn(normalUser);
		Mockito.when(passwordEncryptionService.authenticate(Mockito.anyString(), Mockito.any(byte[].class), Mockito.any(byte[].class)))
				.thenReturn(true);
		Mockito.doNothing().when(postLoginActivities).executeFor(Mockito.any(User.class));

		Optional<User> authenticatedUser = userService.login("test", "password");

		Assert.assertTrue(authenticatedUser.isPresent());
		Assert.assertEquals(normalUser, authenticatedUser.get());
	}

	@Test
	public void apiLoginShouldNotReturnUserIfApikeyNull() {
		Optional<User> user = userService.login(null);
		Assert.assertFalse(user.isPresent());
	}

	@Test
	public void apiLoginShouldLookupUserByApikey() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(null);
		userService.login("apikey");
		Mockito.verify(userDAO).findByApiKey("apikey");
	}

	@Test
	public void apiLoginShouldNotReturnUserIfUserNotFoundFromLookupByApikey() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(null);
		Optional<User> user = userService.login("apikey");
		Assert.assertFalse(user.isPresent());
	}

	@Test
	public void apiLoginShouldNotReturnUserIfUserFoundFromApikeyLookupIsDisabled() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(disabledUser);
		Optional<User> user = userService.login("apikey");
		Assert.assertFalse(user.isPresent());
	}

	@Test
	public void apiLoginShouldPerformPostLoginActivitiesIfUserFoundFromApikeyLookupNotDisabled() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(normalUser);
		userService.login("apikey");
		Mockito.verify(postLoginActivities).executeFor(normalUser);
	}

	@Test
	public void apiLoginShouldReturnUserIfUserFoundFromApikeyLookupNotDisabled() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(normalUser);
		Optional<User> returnedUser = userService.login("apikey");
		Assert.assertEquals(normalUser, returnedUser.get());
	}

}
