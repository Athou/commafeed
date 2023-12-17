package com.commafeed.backend.dao;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUser;
import com.commafeed.backend.model.User;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UserDAO extends GenericDAO<User> {

	private final QUser user = QUser.user;

	@Inject
	public UserDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public User findByName(String name) {
		return query().selectFrom(user).where(user.name.equalsIgnoreCase(name)).fetchOne();
	}

	public User findByApiKey(String key) {
		return query().selectFrom(user).where(user.apiKey.equalsIgnoreCase(key)).fetchOne();
	}

	public User findByEmail(String email) {
		return query().selectFrom(user).where(user.email.equalsIgnoreCase(email)).fetchOne();
	}

	public long count() {
		return query().select(user.count()).from(user).fetchOne();
	}
}
