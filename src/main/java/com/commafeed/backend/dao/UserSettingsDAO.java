package com.commafeed.backend.dao;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings_;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class UserSettingsDAO extends GenericDAO<UserSettings> {

	public UserSettings findByUser(User user) {

		EasyCriteria<UserSettings> criteria = EasyCriteriaFactory
				.createQueryCriteria(em, getType());
		criteria.andEquals(UserSettings_.user.getName(), user);

		UserSettings settings = null;
		try {
			settings = criteria.getSingleResult();
		} catch (NoResultException e) {
			settings = null;
		}
		return settings;
	}
}
