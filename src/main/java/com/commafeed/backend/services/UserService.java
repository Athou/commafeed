package com.commafeed.backend.services;

import java.util.Collection;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;

@Stateless
public class UserService {

	@Inject
	UserDAO userDAO;

	@Inject
	PasswordEncryptionService encryptionService;

	public User login(String name, String password) {
		User user = userDAO.findByName(name);
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
		return register(name, password, null, roles);
	}

	public User register(String name, String password, String email,
			Collection<Role> roles) {
		if (userDAO.findByName(name) != null) {
			return null;
		}
		User user = new User();
		byte[] salt = encryptionService.generateSalt();
		user.setName(name);
		user.setEmail(email);
		user.setSalt(salt);
		user.setPassword(encryptionService.getEncryptedPassword(password, salt));
		user.getRoles().add(new UserRole(user, Role.USER));
		for (Role role : roles) {
			user.getRoles().add(new UserRole(user, role));
			user.getRoles().add(new UserRole(user, role));
		}
		userDAO.save(user);
		return user;
	}
}
