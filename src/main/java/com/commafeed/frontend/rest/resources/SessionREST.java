package com.commafeed.frontend.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.frontend.model.ProfileModificationRequest;
import com.commafeed.frontend.model.UserModel;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/session")
@Api(value = "/session", description = "Operations about user profile")
public class SessionREST extends AbstractREST {

	@Path("/get")
	@GET
	@ApiOperation(value = "Retrieve user's profile", responseClass = "com.commafeed.frontend.model.UserModel")
	public UserModel get() {
		User user = getUser();
		UserModel userModel = new UserModel();
		userModel.setId(user.getId());
		userModel.setName(user.getName());
		userModel.setEmail(user.getEmail());
		userModel.setEnabled(!user.isDisabled());
		for (UserRole role : userRoleDAO.findAll(user)) {
			if (role.getRole() == Role.ADMIN) {
				userModel.setAdmin(true);
			}
		}
		return userModel;
	}

	@Path("/save")
	@POST
	@ApiOperation(value = "Save user's profile")
	public Response save(
			@ApiParam(required = true) ProfileModificationRequest request) {
		User user = getUser();
		user.setEmail(request.getEmail());
		if (StringUtils.isNotBlank(request.getPassword())) {
			byte[] password = encryptionService.getEncryptedPassword(
					request.getPassword(), user.getSalt());
			user.setPassword(password);
		}
		userDAO.update(user);
		return Response.ok().build();
	}
}
