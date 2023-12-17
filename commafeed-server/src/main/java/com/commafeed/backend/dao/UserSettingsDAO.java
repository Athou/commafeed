package com.commafeed.backend.dao;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUserSettings;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserSettingsDAO extends GenericDAO<UserSettings> {

	private final QUserSettings settings = QUserSettings.userSettings;

	@Inject
	public UserSettingsDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public UserSettings findByUser(User user) {
		return query().selectFrom(settings).where(settings.user.eq(user)).fetchFirst();
	}
}
