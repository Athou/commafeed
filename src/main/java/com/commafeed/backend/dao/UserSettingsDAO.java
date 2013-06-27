package com.commafeed.backend.dao;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings_;
import com.commafeed.backend.model.User_;

@Stateless
public class UserSettingsDAO extends GenericDAO<UserSettings> {

	public UserSettings findByUser(User user) {

		CriteriaQuery<UserSettings> query = builder.createQuery(getType());
		Root<UserSettings> root = query.from(getType());

		query.where(builder.equal(root.get(UserSettings_.user).get(User_.id),
				user.getId()));

		UserSettings settings = null;
		try {
			settings = cache(em.createQuery(query)).getSingleResult();
		} catch (NoResultException e) {
			settings = null;
		}
		return settings;
	}
}
