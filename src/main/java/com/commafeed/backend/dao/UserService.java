package com.commafeed.backend.dao;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.security.PasswordEncryptionService;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;

@Stateless
@SuppressWarnings("serial")
public class UserService extends GenericDAO<User> {

	@Inject
	PasswordEncryptionService encryptionService;

	public User login(String name, String password) {
		List<User> users = findByField(MF.i(MF.p(User.class).getName()), name);
		User user = Iterables.getFirst(users, null);
		if (user != null && !user.isDisabled()) {
			boolean authenticated = encryptionService.authenticate(password,
					user.getPassword(), user.getSalt());
			if (authenticated) {
				return user;
			}
		}

		return null;
	}

	public User register(String name, String password, Collection<Role> roles) {
		List<User> users = findByField(MF.i(proxy().getName()), name);
		if (!users.isEmpty()) {
			return null;
		}
		User user = new User();
		byte[] salt = encryptionService.generateSalt();
		user.setName(name);
		user.setSalt(salt);
		user.setPassword(encryptionService.getEncryptedPassword(password, salt));
		user.getRoles().add(new UserRole(user, Role.USER));
		for (Role role : roles) {
			user.getRoles().add(new UserRole(user, role));
			user.getRoles().add(new UserRole(user, role));
		}
		save(user);
		return user;
	}
}
