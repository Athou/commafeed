package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
@Stateless
public class UserRoleService extends GenericDAO<UserRole, Long> {

	public List<String> getRoles(User user) {
		List<String> list = Lists.newArrayList();
		for (UserRole role : findByField(MF.i(proxy().getUser()), user)) {
			list.add(role.getRole());
		}
		return list;
	}
}
