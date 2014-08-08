package com.commafeed.backend.dao;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;

public class UserSettingsDAO extends GenericDAO<UserSettings> {

	public UserSettingsDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public UserSettings findByUser(User user) {
		return uniqueResult(criteria().add(Restrictions.eq("user", user)));
	}
}
