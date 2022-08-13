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
		return query().selectFrom(role).leftJoin(role.user).fetchJoin().distinct().fetch();
	}

	public List<UserRole> findAll(User user) {
		return query().selectFrom(role).where(role.user.eq(user)).distinct().fetch();
	}

	public Set<Role> findRoles(User user) {
		return findAll(user).stream().map(r -> r.getRole()).collect(Collectors.toSet());
	}
}
