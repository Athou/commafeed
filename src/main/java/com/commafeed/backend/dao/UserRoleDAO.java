package com.commafeed.backend.dao;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUserRole;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;

@Singleton
public class UserRoleDAO extends GenericDAO<UserRole> {

	private QUserRole role = QUserRole.userRole;

	@Inject
	public UserRoleDAO(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public List<UserRole> findAll() {
		return newQuery().from(role).leftJoin(role.user).fetch().distinct().list(role);
	}

	public List<UserRole> findAll(User user) {
		return newQuery().from(role).where(role.user.eq(user)).distinct().list(role);
	}

	public Set<Role> findRoles(User user) {
		return findAll(user).stream().map(r -> r.getRole()).collect(Collectors.toSet());
	}
}
