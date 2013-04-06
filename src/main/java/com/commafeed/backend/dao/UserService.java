package com.commafeed.backend.dao;

import java.util.Collection;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.security.PasswordEncryptionService;

@Stateless
@SuppressWarnings("serial")
public class UserService extends GenericDAO<User> {

	@Inject
	PasswordEncryptionService encryptionService;

	public User findByName(String name) {
		TypedQuery<User> query = em.createNamedQuery("User.byName", User.class);
		query.setParameter("name", name.toLowerCase());

		User user = null;
		try {
			user = query.getSingleResult();
		} catch (NoResultException e) {
			user = null;
		}
		return user;
	}

	public User login(String name, String password) {
		User user = findByName(name);
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
		if (findByName(name) != null) {
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
		save(user);
		return user;
	}
}
