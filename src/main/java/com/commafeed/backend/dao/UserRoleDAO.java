package com.commafeed.backend.dao;

import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;

import com.commafeed.backend.model.QUserRole;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.google.common.collect.Sets;

public class UserRoleDAO extends GenericDAO<UserRole> {

	private QUserRole role = QUserRole.userRole;

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
		Set<Role> list = Sets.newHashSet();
		for (UserRole role : findAll(user)) {
			list.add(role.getRole());
		}
		return list;
	}
}
