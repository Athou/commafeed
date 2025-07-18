package com.commafeed.backend.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.internal.PostLoginActivities;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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

	@BeforeEach
	void init() {
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
	void callingLoginShouldNotReturnUserObjectWhenGivenNullNameOrEmail() {
		Optional<User> user = userService.login(null, "password");
		Assertions.assertFalse(user.isPresent());
	}

	@Test
	void callingLoginShouldNotReturnUserObjectWhenGivenNullPassword() {
		Optional<User> user = userService.login("testusername", null);
		Assertions.assertFalse(user.isPresent());
	}

	@Test
	void callingLoginShouldLookupUserByName() {
		userService.login("test", "password");
		Mockito.verify(userDAO).findByName("test");
	}

	@Test
	void callingLoginShouldLookupUserByEmailIfLookupByNameFailed() {
		Mockito.when(userDAO.findByName("test@test.com")).thenReturn(null);
		userService.login("test@test.com", "password");
		Mockito.verify(userDAO).findByEmail("test@test.com");
	}

	@Test
	void callingLoginShouldNotReturnUserObjectIfCouldNotFindUserByNameOrEmail() {
		Mockito.when(userDAO.findByName("test@test.com")).thenReturn(null);
		Mockito.when(userDAO.findByEmail("test@test.com")).thenReturn(null);

		Optional<User> user = userService.login("test@test.com", "password");

		Assertions.assertFalse(user.isPresent());
	}

	@Test
	void callingLoginShouldNotReturnUserObjectIfUserIsDisabled() {
		Mockito.when(userDAO.findByName("test")).thenReturn(disabledUser);
		Optional<User> user = userService.login("test", "password");
		Assertions.assertFalse(user.isPresent());
	}

	@Test
	void callingLoginShouldTryToAuthenticateUserWhoIsNotDisabled() {
		Mockito.when(userDAO.findByName("test")).thenReturn(normalUser);
		Mockito.when(passwordEncryptionService.authenticate(Mockito.anyString(), Mockito.any(byte[].class), Mockito.any(byte[].class)))
				.thenReturn(false);

		userService.login("test", "password");

		Mockito.verify(passwordEncryptionService).authenticate("password", ENCRYPTED_PASSWORD, SALT);
	}

	@Test
	void callingLoginShouldNotReturnUserObjectOnUnsuccessfulAuthentication() {
		Mockito.when(userDAO.findByName("test")).thenReturn(normalUser);
		Mockito.when(passwordEncryptionService.authenticate(Mockito.anyString(), Mockito.any(byte[].class), Mockito.any(byte[].class)))
				.thenReturn(false);

		Optional<User> authenticatedUser = userService.login("test", "password");

		Assertions.assertFalse(authenticatedUser.isPresent());
	}

	@Test
	void callingLoginShouldExecutePostLoginActivitiesForUserOnSuccessfulAuthentication() {
		Mockito.when(userDAO.findByName("test")).thenReturn(normalUser);
		Mockito.when(passwordEncryptionService.authenticate(Mockito.anyString(), Mockito.any(byte[].class), Mockito.any(byte[].class)))
				.thenReturn(true);
		Mockito.doNothing().when(postLoginActivities).executeFor(Mockito.any(User.class));

		userService.login("test", "password");

		Mockito.verify(postLoginActivities).executeFor(normalUser);
	}

	@Test
	void callingLoginShouldReturnUserObjectOnSuccessfulAuthentication() {
		Mockito.when(userDAO.findByName("test")).thenReturn(normalUser);
		Mockito.when(passwordEncryptionService.authenticate(Mockito.anyString(), Mockito.any(byte[].class), Mockito.any(byte[].class)))
				.thenReturn(true);
		Mockito.doNothing().when(postLoginActivities).executeFor(Mockito.any(User.class));

		Optional<User> authenticatedUser = userService.login("test", "password");

		Assertions.assertTrue(authenticatedUser.isPresent());
		Assertions.assertEquals(normalUser, authenticatedUser.get());
	}

	@Test
	void apiLoginShouldNotReturnUserIfApikeyNull() {
		Optional<User> user = userService.login(null);
		Assertions.assertFalse(user.isPresent());
	}

	@Test
	void apiLoginShouldLookupUserByApikey() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(null);
		userService.login("apikey");
		Mockito.verify(userDAO).findByApiKey("apikey");
	}

	@Test
	void apiLoginShouldNotReturnUserIfUserNotFoundFromLookupByApikey() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(null);
		Optional<User> user = userService.login("apikey");
		Assertions.assertFalse(user.isPresent());
	}

	@Test
	void apiLoginShouldNotReturnUserIfUserFoundFromApikeyLookupIsDisabled() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(disabledUser);
		Optional<User> user = userService.login("apikey");
		Assertions.assertFalse(user.isPresent());
	}

	@Test
	void apiLoginShouldPerformPostLoginActivitiesIfUserFoundFromApikeyLookupNotDisabled() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(normalUser);
		userService.login("apikey");
		Mockito.verify(postLoginActivities).executeFor(normalUser);
	}

	@Test
	void apiLoginShouldReturnUserIfUserFoundFromApikeyLookupNotDisabled() {
		Mockito.when(userDAO.findByApiKey("apikey")).thenReturn(normalUser);
		Optional<User> returnedUser = userService.login("apikey");
		Assertions.assertEquals(normalUser, returnedUser.get());
	}

}
