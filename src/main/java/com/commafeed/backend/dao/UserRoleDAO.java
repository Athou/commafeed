package com.commafeed.backend.dao;

import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.model.UserRole_;
import com.google.common.collect.Sets;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@Stateless
public class UserRoleDAO extends GenericDAO<UserRole> {

	@Override
	public List<UserRole> findAll() {
		EasyCriteria<UserRole> criteria = EasyCriteriaFactory
				.createQueryCriteria(em, getType());
		criteria.setDistinctTrue();
		criteria.leftJoinFetch(UserRole_.user.getName());
		return criteria.getResultList();
	}

	public List<UserRole> findAll(User user) {
		return findByField(UserRole_.user, user);
	}

	public Set<Role> findRoles(User user) {
		Set<Role> list = Sets.newHashSet();
		for (UserRole role : findByField(UserRole_.user, user)) {
			list.add(role.getRole());
		}
		return list;
	}
}
