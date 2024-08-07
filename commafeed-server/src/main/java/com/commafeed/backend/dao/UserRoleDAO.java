package com.commafeed.backend.dao;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.commafeed.backend.model.QUserRole;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;

import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

@Singleton
public class UserRoleDAO extends GenericDAO<UserRole> {

	private static final QUserRole ROLE = QUserRole.userRole;

	public UserRoleDAO(EntityManager entityManager) {
		super(entityManager, UserRole.class);
	}

	public List<UserRole> findAll() {
		return query().selectFrom(ROLE).leftJoin(ROLE.user).fetchJoin().distinct().fetch();
	}

	public List<UserRole> findAll(User user) {
		return query().selectFrom(ROLE).where(ROLE.user.eq(user)).distinct().fetch();
	}

	public Set<Role> findRoles(User user) {
		return findAll(user).stream().map(UserRole::getRole).collect(Collectors.toSet());
	}
}
