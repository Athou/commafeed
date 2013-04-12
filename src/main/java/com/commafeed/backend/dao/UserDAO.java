package com.commafeed.backend.dao;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.User_;
import com.commafeed.backend.services.PasswordEncryptionService;

@Stateless
@SuppressWarnings("serial")
public class UserDAO extends GenericDAO<User> {

	@Inject
	PasswordEncryptionService encryptionService;

	public User findByName(String name) {

		CriteriaQuery<User> query = builder.createQuery(getType());
		Root<User> root = query.from(getType());
		query.where(builder.equal(builder.lower(root.get(User_.name)),
				name.toLowerCase()));
		TypedQuery<User> q = em.createQuery(query);

		User user = null;
		try {
			user = q.getSingleResult();
		} catch (NoResultException e) {
			user = null;
		}
		return user;
	}

}
