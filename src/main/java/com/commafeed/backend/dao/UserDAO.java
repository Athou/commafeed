package com.commafeed.backend.dao;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.User_;

@Stateless
public class UserDAO extends GenericDAO<User> {

	public User findByName(String name) {

		CriteriaQuery<User> query = builder.createQuery(getType());
		Root<User> root = query.from(getType());
		query.where(builder.equal(builder.lower(root.get(User_.name)),
				name.toLowerCase()));
		TypedQuery<User> q = em.createQuery(query);
		cache(q);
		
		User user = null;
		try {
			user = q.getSingleResult();
		} catch (NoResultException e) {
			user = null;
		}
		return user;
	}

	public User findByApiKey(String key) {
		CriteriaQuery<User> query = builder.createQuery(getType());
		Root<User> root = query.from(getType());
		query.where(builder.equal(root.get(User_.apiKey), key));
		TypedQuery<User> q = em.createQuery(query);
		cache(q);
		
		User user = null;
		try {
			user = q.getSingleResult();
		} catch (NoResultException e) {
			user = null;
		}
		return user;
	}

	public User findByEmail(String email) {
		if (StringUtils.isBlank(email)) {
			return null;
		}
		CriteriaQuery<User> query = builder.createQuery(getType());
		Root<User> root = query.from(getType());
		query.where(builder.equal(root.get(User_.email), email));
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
