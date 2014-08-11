package com.commafeed.backend.dao;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUserSettings;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;

public class UserSettingsDAO extends GenericDAO<UserSettings> {

	private QUserSettings settings = QUserSettings.userSettings;

	public UserSettingsDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public UserSettings findByUser(User user) {
		return newQuery().from(settings).where(settings.user.eq(user)).uniqueResult(settings);
	}
}
