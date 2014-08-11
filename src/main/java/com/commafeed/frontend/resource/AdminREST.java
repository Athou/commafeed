package com.commafeed.frontend.resource;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.AllArgsConstructor;

import org.apache.commons.lang.StringUtils;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.ApplicationSettings;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.DatabaseCleaningService;
import com.commafeed.backend.service.PasswordEncryptionService;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.auth.SecurityCheck;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.IDRequest;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/admin")
@Api(value = "/admin", description = "Operations about application administration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AllArgsConstructor
public class AdminREST {

	private final UserDAO userDAO;
	private final UserRoleDAO userRoleDAO;
	private final UserService userService;
	private final PasswordEncryptionService encryptionService;
	private final DatabaseCleaningService cleaner;
	private final CommaFeedConfiguration config;
	private final MetricRegistry metrics;

	@Path("/user/save")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Save or update a user", notes = "Save or update a user. If the id is not specified, a new user will be created")
	public Response save(@SecurityCheck(Role.ADMIN) User user, @ApiParam(required = true) UserModel userModel) {
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
			User u = userDAO.findById(id);
			if (CommaFeedApplication.USERNAME_ADMIN.equals(u.getName()) && !userModel.isEnabled()) {
				return Response.status(Status.FORBIDDEN).entity("You cannot disable the admin user.").build();
			}
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
	@ApiOperation(value = "Get user information", notes = "Get user information", response = UserModel.class)
	public Response getUser(@SecurityCheck(Role.ADMIN) User user, @ApiParam(value = "user id", required = true) @PathParam("id") Long id) {
		Preconditions.checkNotNull(id);
		User u = userDAO.findById(id);
		UserModel userModel = new UserModel();
		userModel.setId(u.getId());
		userModel.setName(u.getName());
		userModel.setEmail(u.getEmail());
		userModel.setEnabled(!u.isDisabled());
		for (UserRole role : userRoleDAO.findAll(u)) {
			if (role.getRole() == Role.ADMIN) {
				userModel.setAdmin(true);
			}
		}
		return Response.ok(userModel).build();
	}

	@Path("/user/getAll")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Get all users", notes = "Get all users", response = UserModel.class, responseContainer = "List")
	public Response getUsers(@SecurityCheck(Role.ADMIN) User user) {
		Map<Long, UserModel> users = Maps.newHashMap();
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
	@ApiOperation(value = "Delete a user", notes = "Delete a user, and all his subscriptions")
	public Response delete(@SecurityCheck(Role.ADMIN) User user, @ApiParam(required = true) IDRequest req) {
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
	@ApiOperation(value = "Retrieve application settings", notes = "Retrieve application settings", response = ApplicationSettings.class)
	public Response getSettings(@SecurityCheck(Role.ADMIN) User user) {
		return Response.ok(config.getApplicationSettings()).build();
	}

	@Path("/metrics")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Retrieve server metrics")
	public Response getMetrics(@SecurityCheck(Role.ADMIN) User user) {
		return Response.ok(metrics).build();
	}

	@Path("/cleanup/entries")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Entries cleanup", notes = "Delete entries without subscriptions")
	public Response cleanupEntries(@SecurityCheck(Role.ADMIN) User user) {
		Map<String, Long> map = Maps.newHashMap();
		map.put("entries_without_subscriptions", cleaner.cleanEntriesWithoutSubscriptions());
		return Response.ok(map).build();
	}

	@Path("/cleanup/feeds")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Feeds cleanup", notes = "Delete feeds without subscriptions")
	public Response cleanupFeeds(@SecurityCheck(Role.ADMIN) User user) {
		Map<String, Long> map = Maps.newHashMap();
		map.put("feeds_without_subscriptions", cleaner.cleanFeedsWithoutSubscriptions());
		return Response.ok(map).build();
	}

	@Path("/cleanup/content")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Content cleanup", notes = "Delete contents without entries")
	public Response cleanupContents(@SecurityCheck(Role.ADMIN) User user) {
		Map<String, Long> map = Maps.newHashMap();
		map.put("contents_without_entries", cleaner.cleanContentsWithoutEntries());
		return Response.ok(map).build();
	}

}
