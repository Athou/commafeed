package com.commafeed.backend.dao;

import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Sets;

@SuppressWarnings("serial")
@Stateless
public class UserRoleDAO extends GenericDAO<UserRole> {

	public List<UserRole> findAll(User user) {
		return findByField(MF.i(MF.p(UserRole.class).getUser()), user);
	}

	public Set<Role> findRoles(User user) {
		Set<Role> list = Sets.newHashSet();
		for (UserRole role : findByField(MF.i(proxy().getUser()), user)) {
			list.add(role.getRole());
		}
		return list;
	}
}
