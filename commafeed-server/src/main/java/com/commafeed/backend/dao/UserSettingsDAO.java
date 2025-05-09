package com.commafeed.backend.dao;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

import com.commafeed.backend.model.QUserSettings;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;

@Singleton
public class UserSettingsDAO extends GenericDAO<UserSettings> {

	private static final QUserSettings SETTINGS = QUserSettings.userSettings;

	public UserSettingsDAO(EntityManager entityManager) {
		super(entityManager, UserSettings.class);
	}

	public UserSettings findByUser(User user) {
		return query().selectFrom(SETTINGS).where(SETTINGS.user.eq(user)).fetchFirst();
	}
}
