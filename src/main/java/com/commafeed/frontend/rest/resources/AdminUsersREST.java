package com.commafeed.frontend.rest.resources;

import java.util.Collection;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.security.Role;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.rest.SecurityCheck;
import com.google.common.collect.Maps;

@SecurityCheck(Role.ADMIN)
@Path("admin/users")
public class AdminUsersREST extends AbstractREST {

	@Path("get")
	@GET
	public Collection<UserModel> getUsers() {
		Map<Long, UserModel> users = Maps.newHashMap();
		for (UserRole role : userRoleService.findAll()) {
			User user = role.getUser();
			Long key = user.getId();
			UserModel userModel = users.get(key);
			if (userModel == null) {
				userModel = new UserModel();
				userModel.setName(user.getName());
				userModel.setEnabled(!user.isDisabled());
				users.put(key, userModel);
			}
			userModel.getRoles().add(role.getRole());
		}
		return users.values();
	}
}
