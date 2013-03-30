package com.commafeed.frontend.rest.resources;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.security.Role;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.rest.SecurityCheck;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SecurityCheck(Role.ADMIN)
@Path("admin/users")
public class AdminUsersREST extends AbstractREST {

	@Path("save")
	@POST
	public Response save(UserModel userModel) {
		Preconditions.checkNotNull(userModel);
		Preconditions.checkNotNull(userModel.getName());

		Long id = userModel.getId();
		if (id == null) {
			Preconditions.checkNotNull(userModel.getPassword());

			Set<String> roles = Sets.newHashSet(Role.USER);
			if (userModel.isAdmin()) {
				roles.add(Role.ADMIN);
			}

			User user = userService.register(userModel.getName(),
					userModel.getPassword(), roles);
			if (user == null) {
				return Response.status(Status.CONFLICT)
						.entity("User already exists.").build();
			}
		} else {
			User user = userService.findById(id);
			user.setName(userModel.getName());
			if (StringUtils.isNotBlank(userModel.getPassword())) {
				user.setPassword(encryptionService.getEncryptedPassword(
						userModel.getPassword(), user.getSalt()));
			}
			user.setDisabled(!userModel.isEnabled());
			userService.update(user);

			Set<String> roles = userRoleService.getRoles(user);
			if (userModel.isAdmin() && !roles.contains(Role.ADMIN)) {
				userRoleService.save(new UserRole(user, Role.ADMIN));
			} else if (!userModel.isAdmin() && roles.contains(Role.ADMIN)) {
				for (UserRole userRole : userRoleService.findAll(user)) {
					if (Role.ADMIN.equals(userRole.getRole())) {
						userRoleService.delete(userRole);
					}
				}
			}

		}
		return Response.ok(Status.OK).entity("OK").build();

	}

	@Path("get")
	@GET
	public UserModel getUser(@QueryParam("id") Long id) {
		User user = userService.findById(id);
		UserModel userModel = new UserModel();
		userModel.setId(user.getId());
		userModel.setName(user.getName());
		userModel.setEnabled(!user.isDisabled());
		for (UserRole role : userRoleService.findAll(user)) {
			if (Role.ADMIN.equals(role.getRole())) {
				userModel.setAdmin(true);
			}
		}
		return userModel;
	}

	@Path("getAll")
	@GET
	public Collection<UserModel> getUsers() {
		Map<Long, UserModel> users = Maps.newHashMap();
		for (UserRole role : userRoleService.findAll()) {
			User user = role.getUser();
			Long key = user.getId();
			UserModel userModel = users.get(key);
			if (userModel == null) {
				userModel = new UserModel();
				userModel.setId(user.getId());
				userModel.setName(user.getName());
				userModel.setEnabled(!user.isDisabled());
				users.put(key, userModel);
			}
			if (Role.ADMIN.equals(role.getRole())) {
				userModel.setAdmin(true);
			}
		}
		return users.values();
	}
}
