package com.commafeed.backend.dao;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings_;

@Stateless
public class UserSettingsDAO extends GenericDAO<UserSettings> {

	public UserSettings findByUser(User user) {

		CriteriaQuery<UserSettings> query = builder.createQuery(getType());
		Root<UserSettings> root = query.from(getType());

		query.where(builder.equal(root.get(UserSettings_.user), user));

		UserSettings settings = null;
		try {
			settings = em.createQuery(query).getSingleResult();
		} catch (NoResultException e) {
			settings = null;
		}
		return settings;
	}
}
