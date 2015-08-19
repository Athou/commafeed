package com.commafeed.backend.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUserSettings;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;

@Singleton
public class UserSettingsDAO extends GenericDAO<UserSettings> {

	private QUserSettings settings = QUserSettings.userSettings;

	@Inject
	public UserSettingsDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public UserSettings findByUser(User user) {
		return query().selectFrom(settings).where(settings.user.eq(user)).fetchFirst();
	}
}
