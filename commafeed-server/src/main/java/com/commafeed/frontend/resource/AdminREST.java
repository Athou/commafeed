package com.commafeed.frontend.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConstants;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.PasswordEncryptionService;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.AdminSaveUserRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Path("/rest/admin")
@RolesAllowed(Roles.ADMIN)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
@Tag(name = "Admin")
public class AdminREST {

	private final AuthenticationContext authenticationContext;
	private final UserDAO userDAO;
	private final UserRoleDAO userRoleDAO;
	private final UserService userService;
	private final PasswordEncryptionService encryptionService;
	private final MetricRegistry metrics;

	@Path("/user/save")
	@POST
	@Transactional
	@Operation(
			summary = "Save or update a user",
			description = "Save or update a user. If the id is not specified, a new user will be created")
	public Response adminSaveUser(@Parameter(required = true) AdminSaveUserRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getName());

		Long id = req.getId();
		if (id == null) {
			Preconditions.checkNotNull(req.getPassword());

			Set<Role> roles = Sets.newHashSet(Role.USER);
			if (req.isAdmin()) {
				roles.add(Role.ADMIN);
			}
			try {
				id = userService.register(req.getName(), req.getPassword(), req.getEmail(), roles, true).getId();
			} catch (Exception e) {
				return Response.status(Status.CONFLICT).entity(e.getMessage()).build();
			}
		} else {
			User user = authenticationContext.getCurrentUser();
			if (req.getId().equals(user.getId()) && !req.isEnabled()) {
				return Response.status(Status.FORBIDDEN).entity("You cannot disable your own account.").build();
			}

			User u = userDAO.findById(id);
			u.setName(req.getName());
			if (StringUtils.isNotBlank(req.getPassword())) {
				u.setPassword(encryptionService.getEncryptedPassword(req.getPassword(), u.getSalt()));
			}
			u.setEmail(req.getEmail());
			u.setDisabled(!req.isEnabled());

			Set<Role> roles = userRoleDAO.findRoles(u);
			if (req.isAdmin() && !roles.contains(Role.ADMIN)) {
				userRoleDAO.persist(new UserRole(u, Role.ADMIN));
			} else if (!req.isAdmin() && roles.contains(Role.ADMIN)) {
				if (CommaFeedConstants.USERNAME_ADMIN.equals(u.getName())) {
					return Response.status(Status.FORBIDDEN).entity("You cannot remove the admin role from the admin user.").build();
				}
				for (UserRole userRole : userRoleDAO.findAll(u)) {
					if (userRole.getRole() == Role.ADMIN) {
						userRoleDAO.delete(userRole);
					}
				}
			}

		}
		return Response.ok(id).build();

	}

	@Path("/user/get/{id}")
	@GET
	@Transactional
	@Operation(
			summary = "Get user information",
			description = "Get user information",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = UserModel.class))) })
	public Response adminGetUser(@Parameter(description = "user id", required = true) @PathParam("id") Long id) {
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
	@Transactional
	@Operation(
			summary = "Get all users",
			description = "Get all users",
			responses = { @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserModel.class)))) })
	public Response adminGetUsers() {
		Map<Long, UserModel> users = new HashMap<>();
		for (UserRole role : userRoleDAO.findAll()) {
			User u = role.getUser();
			UserModel userModel = users.computeIfAbsent(u.getId(), k -> {
				UserModel um = new UserModel();
				um.setId(u.getId());
				um.setName(u.getName());
				um.setEmail(u.getEmail());
				um.setEnabled(!u.isDisabled());
				um.setCreated(u.getCreated());
				um.setLastLogin(u.getLastLogin());
				return um;
			});

			if (role.getRole() == Role.ADMIN) {
				userModel.setAdmin(true);
			}
		}
		return Response.ok(users.values()).build();
	}

	@Path("/user/delete")
	@POST
	@Transactional
	@Operation(summary = "Delete a user", description = "Delete a user, and all his subscriptions")
	public Response adminDeleteUser(@Parameter(required = true) IDRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		User u = userDAO.findById(req.getId());
		if (u == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		User user = authenticationContext.getCurrentUser();
		if (user.getId().equals(u.getId())) {
			return Response.status(Status.FORBIDDEN).entity("You cannot delete your own user.").build();
		}
		userService.unregister(u);
		return Response.ok().build();
	}

	@Path("/metrics")
	@GET
	@Transactional
	@Operation(summary = "Retrieve server metrics")
	public Response getMetrics() {
		return Response.ok(metrics).build();
	}

}
