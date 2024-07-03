package com.commafeed.backend.dao;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUserSettings;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserSettingsDAO extends GenericDAO<UserSettings> {

	private static final QUserSettings SETTINGS = QUserSettings.userSettings;

	@Inject
	public UserSettingsDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public UserSettings findByUser(User user) {
		return query().selectFrom(SETTINGS).where(SETTINGS.user.eq(user)).fetchFirst();
	}
}
