package com.commafeed.backend.service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

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
	 * should triggers after successful login
	 */
	public void performPostLoginActivities(User user) {
		postLoginActivities.executeFor(user);
	}

	public User register(String name, String password, String email, Collection<Role> roles) {
		return register(name, password, email, roles, false);
	}

	public User register(String name, String password, String email, Collection<Role> roles, boolean forceRegistration) {

		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(StringUtils.length(name) <= 32, "Name too long (32 characters maximum)");
		Preconditions.checkNotNull(password);

		if (!forceRegistration) {
			Preconditions.checkState(config.getApplicationSettings().getAllowRegistrations(),
					"Registrations are closed on this CommaFeed instance");

			Preconditions.checkNotNull(email);
			Preconditions.checkArgument(StringUtils.length(name) >= 3, "Name too short (3 characters minimum)");
			Preconditions
					.checkArgument(forceRegistration || StringUtils.length(password) >= 6, "Password too short (6 characters maximum)");
			Preconditions.checkArgument(StringUtils.contains(email, "@"), "Invalid email address");
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

	public void unregister(User user) {
		feedCategoryDAO.delete(feedCategoryDAO.findAll(user));
		userSettingsDAO.delete(userSettingsDAO.findByUser(user));
		userRoleDAO.delete(userRoleDAO.findAll(user));
		feedSubscriptionDAO.delete(feedSubscriptionDAO.findAll(user));
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
