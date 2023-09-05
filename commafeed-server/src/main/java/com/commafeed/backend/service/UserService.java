package com.commafeed.backend.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.internal.PostLoginActivities;
import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class UserService {

	private final FeedCategoryDAO feedCategoryDAO;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final UserDAO userDAO;
	private final UserRoleDAO userRoleDAO;
	private final UserSettingsDAO userSettingsDAO;

	private final PasswordEncryptionService encryptionService;
	private final CommaFeedConfiguration config;

	private final PostLoginActivities postLoginActivities;

	/**
	 * try to log in with given credentials
	 */
	public Optional<User> login(String nameOrEmail, String password) {
		if (nameOrEmail == null || password == null) {
			return Optional.empty();
		}

		User user = userDAO.findByName(nameOrEmail);
		if (user == null) {
			user = userDAO.findByEmail(nameOrEmail);
		}
		if (user != null && !user.isDisabled()) {
			boolean authenticated = encryptionService.authenticate(password, user.getPassword(), user.getSalt());
			if (authenticated) {
				performPostLoginActivities(user);
				return Optional.of(user);
			}
		}
		return Optional.empty();
	}

	/**
	 * try to log in with given api key
	 */
	public Optional<User> login(String apiKey) {
		if (apiKey == null) {
			return Optional.empty();
		}

		User user = userDAO.findByApiKey(apiKey);
		if (user != null && !user.isDisabled()) {
			performPostLoginActivities(user);
			return Optional.of(user);
		}
		return Optional.empty();
	}

	/**
	 * try to log in with given fever api key
	 */
	public Optional<User> login(long userId, String feverApiKey) {
		if (feverApiKey == null) {
			return Optional.empty();
		}

		User user = userDAO.findById(userId);
		if (user == null || user.isDisabled() || user.getApiKey() == null) {
			return Optional.empty();
		}

		String computedFeverApiKey = DigestUtils.md5Hex(user.getName() + ":" + user.getApiKey());
		if (!computedFeverApiKey.equals(feverApiKey)) {
			return Optional.empty();
		}

		performPostLoginActivities(user);
		return Optional.of(user);
	}

	/**
	 * should triggers after successful login
	 */
	public void performPostLoginActivities(User user) {
		postLoginActivities.executeFor(user);
	}

	public User register(String name, String password, String email, Collection<Role> roles) {
		return register(name, password, email, roles, false);
	}

	public User register(String name, String password, String email, Collection<Role> roles, boolean forceRegistration) {

		if (!forceRegistration) {
			Preconditions.checkState(config.getApplicationSettings().getAllowRegistrations(),
					"Registrations are closed on this CommaFeed instance");
		}

		Preconditions.checkArgument(userDAO.findByName(name) == null, "Name already taken");
		if (StringUtils.isNotBlank(email)) {
			Preconditions.checkArgument(userDAO.findByEmail(email) == null, "Email already taken");
		}

		User user = new User();
		byte[] salt = encryptionService.generateSalt();
		user.setName(name);
		user.setEmail(email);
		user.setCreated(new Date());
		user.setSalt(salt);
		user.setPassword(encryptionService.getEncryptedPassword(password, salt));
		userDAO.saveOrUpdate(user);
		for (Role role : roles) {
			userRoleDAO.saveOrUpdate(new UserRole(user, role));
		}
		return user;
	}

	public void createAdminUser() {
		register(CommaFeedApplication.USERNAME_ADMIN, "admin", "admin@commafeed.com", Arrays.asList(Role.ADMIN, Role.USER), true);
	}

	public void createDemoUser() {
		register(CommaFeedApplication.USERNAME_DEMO, "demo", "demo@commafeed.com", Collections.singletonList(Role.USER), true);
	}

	public void unregister(User user) {
		userSettingsDAO.delete(userSettingsDAO.findByUser(user));
		userRoleDAO.delete(userRoleDAO.findAll(user));
		feedSubscriptionDAO.delete(feedSubscriptionDAO.findAll(user));
		feedCategoryDAO.delete(feedCategoryDAO.findAll(user));
		userDAO.delete(user);
	}

	public String generateApiKey(User user) {
		byte[] key = encryptionService.getEncryptedPassword(UUID.randomUUID().toString(), user.getSalt());
		return DigestUtils.sha1Hex(key);
	}

	public Set<Role> getRoles(User user) {
		return userRoleDAO.findRoles(user);
	}
}
