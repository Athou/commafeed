package com.commafeed.backend.service;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

@RequiredArgsConstructor
public class UserService {

	private static final String SESSION_KEY_USER = "user";

	private final FeedCategoryDAO feedCategoryDAO;
	private final UserDAO userDAO;
	private final UserSettingsDAO userSettingsDAO;

	private final FeedSubscriptionService feedSubscriptionService;
	private final PasswordEncryptionService encryptionService;
	private final CommaFeedConfiguration config;

	/**
	 * try to log in with given credentials
	 */
	public Optional<User> login(String nameOrEmail, String password) {
		if (nameOrEmail == null || password == null) {
			return Optional.absent();
		}

		User user = userDAO.findByName(nameOrEmail);
		if (user == null) {
			user = userDAO.findByEmail(nameOrEmail);
		}
		if (user != null && !user.isDisabled()) {
			boolean authenticated = encryptionService.authenticate(password, user.getPassword(), user.getSalt());
			if (authenticated) {
				afterLogin(user);
				return Optional.fromNullable(user);
			}
		}
		return Optional.absent();
	}

	/**
	 * try to log in with given credentials and create a session for the user
	 */
	public Optional<User> login(String nameOrEmail, String password, HttpSession sessionToFill) {
		Optional<User> user = login(nameOrEmail, password);
		if (user.isPresent()) {
			sessionToFill.setAttribute(SESSION_KEY_USER, user.get());
		}
		return user;
	}

	/**
	 * try to log in by checking if the user has an active session
	 */
	public Optional<User> login(HttpSession session) {
		User user = (User) session.getAttribute(SESSION_KEY_USER);
		if (user != null) {
			afterLogin(user);
			return Optional.of(user);
		}
		return Optional.absent();
	}

	/**
	 * try to log in with given api key
	 */
	public Optional<User> login(String apiKey) {
		if (apiKey == null) {
			return Optional.absent();
		}

		User user = userDAO.findByApiKey(apiKey);
		if (user != null && !user.isDisabled()) {
			afterLogin(user);
			return Optional.fromNullable(user);
		}
		return Optional.absent();
	}

	/**
	 * should triggers after successful login
	 */
	private void afterLogin(User user) {
		Date lastLogin = user.getLastLogin();
		Date now = new Date();

		boolean saveUser = false;
		// only update lastLogin field every hour in order to not
		// invalidate the cache everytime someone logs in
		if (lastLogin == null || lastLogin.before(DateUtils.addHours(now, -1))) {
			user.setLastLogin(now);
			saveUser = true;
		}
		if (config.getApplicationSettings().isHeavyLoad()
				&& (user.getLastFullRefresh() == null || user.getLastFullRefresh().before(DateUtils.addMinutes(now, -30)))) {
			user.setLastFullRefresh(now);
			saveUser = true;
			feedSubscriptionService.refreshAll(user);
		}
		if (saveUser) {
			userDAO.merge(user);
		}
	}

	public User register(String name, String password, String email, Collection<Role> roles) {
		return register(name, password, email, roles, false);
	}

	public User register(String name, String password, String email, Collection<Role> roles, boolean forceRegistration) {

		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(StringUtils.length(name) <= 32, "Name too long (32 characters maximum)");
		Preconditions.checkNotNull(password);

		if (!forceRegistration) {
			Preconditions.checkState(config.getApplicationSettings().isAllowRegistrations(),
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
		for (Role role : roles) {
			user.getRoles().add(new UserRole(user, role));
		}
		userDAO.saveOrUpdate(user);
		return user;
	}

	public void unregister(User user) {
		feedCategoryDAO.delete(feedCategoryDAO.findAll(user));
		userSettingsDAO.delete(userSettingsDAO.findByUser(user));
		userDAO.delete(user);
	}

	public String generateApiKey(User user) {
		byte[] key = encryptionService.getEncryptedPassword(UUID.randomUUID().toString(), user.getSalt());
		return DigestUtils.sha1Hex(key);
	}
}
