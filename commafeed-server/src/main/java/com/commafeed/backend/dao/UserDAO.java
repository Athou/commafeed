package com.commafeed.backend.dao;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUser;
import com.commafeed.backend.model.User;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserDAO extends GenericDAO<User> {

	private static final QUser USER = QUser.user;

	@Inject
	public UserDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public User findByName(String name) {
		return query().selectFrom(USER).where(USER.name.equalsIgnoreCase(name)).fetchOne();
	}

	public User findByApiKey(String key) {
		return query().selectFrom(USER).where(USER.apiKey.equalsIgnoreCase(key)).fetchOne();
	}

	public User findByEmail(String email) {
		return query().selectFrom(USER).where(USER.email.equalsIgnoreCase(email)).fetchOne();
	}

	public long count() {
		return query().select(USER.count()).from(USER).fetchOne();
	}
}
