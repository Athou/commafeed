package com.commafeed.frontend.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.frontend.model.UserModel;

@Path("session")
public class SessionREST extends AbstractREST {

	@Path("get")
	@GET
	public UserModel get() {
		User user = getUser();
		UserModel userModel = new UserModel();
		userModel.setId(user.getId());
		userModel.setName(user.getName());
		userModel.setEnabled(!user.isDisabled());
		for (UserRole role : userRoleDAO.findAll(user)) {
			if (role.getRole() == Role.ADMIN) {
				userModel.setAdmin(true);
			}
		}
		return userModel;
	}
}
