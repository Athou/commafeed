package com.commafeed.backend.dao;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.commafeed.backend.model.User;
import com.commafeed.backend.services.PasswordEncryptionService;

@Stateless
@SuppressWarnings("serial")
public class UserDAO extends GenericDAO<User> {

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

}
