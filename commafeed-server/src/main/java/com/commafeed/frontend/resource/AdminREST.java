package com.commafeed.frontend.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.ApplicationSettings;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.PasswordEncryptionService;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.auth.SecurityCheck;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.IDRequest;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class AdminREST {

	private final UserDAO userDAO;
	private final UserRoleDAO userRoleDAO;
	private final UserService userService;
	private final PasswordEncryptionService encryptionService;
	private final CommaFeedConfiguration config;
	private final MetricRegistry metrics;

	@Path("/user/save")
	@POST
	@UnitOfWork
	@Operation(
			summary = "Save or update a user",
			description = "Save or update a user. If the id is not specified, a new user will be created")
	@Timed
	public Response adminSaveUser(@Parameter(hidden = true) @SecurityCheck(Role.ADMIN) User user,
			@Parameter(required = true) UserModel userModel) {
		Preconditions.checkNotNull(userModel);
		Preconditions.checkNotNull(userModel.getName());

		Long id = userModel.getId();
		if (id == null) {
			Preconditions.checkNotNull(userModel.getPassword());

			Set<Role> roles = Sets.newHashSet(Role.USER);
			if (userModel.isAdmin()) {
				roles.add(Role.ADMIN);
			}
			try {
				userService.register(userModel.getName(), userModel.getPassword(), userModel.getEmail(), roles, true);
			} catch (Exception e) {
				return Response.status(Status.CONFLICT).entity(e.getMessage()).build();
			}
		} else {
			if (userModel.getId().equals(user.getId()) && !userModel.isEnabled()) {
				return Response.status(Status.FORBIDDEN).entity("You cannot disable your own account.").build();
			}

			User u = userDAO.findById(id);
			u.setName(userModel.getName());
			if (StringUtils.isNotBlank(userModel.getPassword())) {
				u.setPassword(encryptionService.getEncryptedPassword(userModel.getPassword(), u.getSalt()));
			}
			u.setEmail(userModel.getEmail());
			u.setDisabled(!userModel.isEnabled());
			userDAO.saveOrUpdate(u);

			Set<Role> roles = userRoleDAO.findRoles(u);
			if (userModel.isAdmin() && !roles.contains(Role.ADMIN)) {
				userRoleDAO.saveOrUpdate(new UserRole(u, Role.ADMIN));
			} else if (!userModel.isAdmin() && roles.contains(Role.ADMIN)) {
				if (CommaFeedApplication.USERNAME_ADMIN.equals(u.getName())) {
					return Response.status(Status.FORBIDDEN).entity("You cannot remove the admin role from the admin user.").build();
				}
				for (UserRole userRole : userRoleDAO.findAll(u)) {
					if (userRole.getRole() == Role.ADMIN) {
						userRoleDAO.delete(userRole);
					}
				}
			}

		}
		return Response.ok().build();

	}

	@Path("/user/get/{id}")
	@GET
	@UnitOfWork
	@Operation(
			summary = "Get user information",
			description = "Get user information",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = UserModel.class))) })
	@Timed
	public Response adminGetUser(@Parameter(hidden = true) @SecurityCheck(Role.ADMIN) User user,
			@Parameter(description = "user id", required = true) @PathParam("id") Long id) {
		Preconditions.checkNotNull(id);
		User u = userDAO.findById(id);
		UserModel userModel = new UserModel();
		userModel.setId(u.getId());
		userModel.setName(u.getName());
		userModel.setEmail(u.getEmail());
		userModel.setEnabled(!u.isDisabled());
		userModel.setAdmin(userRoleDAO.findAll(u).stream().anyMatch(r -> r.getRole() == Role.ADMIN));
		return Response.ok(userModel).build();
	}

	@Path("/user/getAll")
	@GET
	@UnitOfWork
	@Operation(
			summary = "Get all users",
			description = "Get all users",
			responses = { @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserModel.class)))) })
	@Timed
	public Response adminGetUsers(@Parameter(hidden = true) @SecurityCheck(Role.ADMIN) User user) {
		Map<Long, UserModel> users = new HashMap<>();
		for (UserRole role : userRoleDAO.findAll()) {
			User u = role.getUser();
			Long key = u.getId();
			UserModel userModel = users.get(key);
			if (userModel == null) {
				userModel = new UserModel();
				userModel.setId(u.getId());
				userModel.setName(u.getName());
				userModel.setEmail(u.getEmail());
				userModel.setEnabled(!u.isDisabled());
				userModel.setCreated(u.getCreated());
				userModel.setLastLogin(u.getLastLogin());
				users.put(key, userModel);
			}
			if (role.getRole() == Role.ADMIN) {
				userModel.setAdmin(true);
			}
		}
		return Response.ok(users.values()).build();
	}

	@Path("/user/delete")
	@POST
	@UnitOfWork
	@Operation(summary = "Delete a user", description = "Delete a user, and all his subscriptions")
	@Timed
	public Response adminDeleteUser(@Parameter(hidden = true) @SecurityCheck(Role.ADMIN) User user,
			@Parameter(required = true) IDRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		User u = userDAO.findById(req.getId());
		if (u == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if (user.getId().equals(u.getId())) {
			return Response.status(Status.FORBIDDEN).entity("You cannot delete your own user.").build();
		}
		userService.unregister(u);
		return Response.ok().build();
	}

	@Path("/settings")
	@GET
	@UnitOfWork
	@Operation(
			summary = "Retrieve application settings",
			description = "Retrieve application settings",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = ApplicationSettings.class))) })
	@Timed
	public Response getApplicationSettings(@Parameter(hidden = true) @SecurityCheck(Role.ADMIN) User user) {
		return Response.ok(config.getApplicationSettings()).build();
	}

	@Path("/metrics")
	@GET
	@UnitOfWork
	@Operation(summary = "Retrieve server metrics")
	@Timed
	public Response getMetrics(@Parameter(hidden = true) @SecurityCheck(Role.ADMIN) User user) {
		return Response.ok(metrics).build();
	}

}
