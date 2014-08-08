package com.commafeed.backend.dao;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUser;
import com.commafeed.backend.model.User;

public class UserDAO extends GenericDAO<User> {

	private QUser user = QUser.user;

	public UserDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public User findByName(String name) {
		return newQuery().from(user).where(user.name.equalsIgnoreCase(name)).uniqueResult(user);
	}

	public User findByApiKey(String key) {
		return newQuery().from(user).where(user.apiKey.equalsIgnoreCase(key)).uniqueResult(user);
	}

	public User findByEmail(String email) {
		return newQuery().from(user).where(user.email.equalsIgnoreCase(email)).uniqueResult(user);
	}
}
