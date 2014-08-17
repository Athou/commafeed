package com.commafeed.backend.dao;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUser;
import com.commafeed.backend.model.QUserRole;
import com.commafeed.backend.model.User;

@Singleton
public class UserDAO extends GenericDAO<User> {

	private QUser user = QUser.user;

	@Inject
	public UserDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public User findByName(String name) {
		return newQuery().from(user).where(user.name.equalsIgnoreCase(name)).leftJoin(user.roles, QUserRole.userRole).fetch()
				.uniqueResult(user);
	}

	public User findByApiKey(String key) {
		return newQuery().from(user).where(user.apiKey.equalsIgnoreCase(key)).leftJoin(user.roles, QUserRole.userRole).fetch()
				.uniqueResult(user);
	}

	public User findByEmail(String email) {
		return newQuery().from(user).where(user.email.equalsIgnoreCase(email)).leftJoin(user.roles, QUserRole.userRole).fetch()
				.uniqueResult(user);
	}

	public long count() {
		return newQuery().from(user).count();
	}
}
