package com.commafeed.frontend.rest.resources;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.DatabaseCleaner;
import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.StartupBean;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedDAO.DuplicateMode;
import com.commafeed.backend.dao.FeedDAO.FeedCount;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.feeds.FeedRefreshTaskGiver;
import com.commafeed.backend.feeds.FeedRefreshUpdater;
import com.commafeed.backend.feeds.FeedRefreshWorker;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.FeedService;
import com.commafeed.backend.services.PasswordEncryptionService;
import com.commafeed.backend.services.UserService;
import com.commafeed.frontend.SecurityCheck;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.FeedMergeRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@SecurityCheck(Role.ADMIN)
@Path("/admin")
@Api(value = "/admin", description = "Operations about application administration")
public class AdminREST extends AbstractResourceREST {

	@Inject
	FeedService feedService;

	@Inject
	UserService userService;

	@Inject
	UserDAO userDAO;

	@Inject
	UserRoleDAO userRoleDAO;

	@Inject
	FeedDAO feedDAO;

	@Inject
	MetricsBean metricsBean;

	@Inject
	DatabaseCleaner cleaner;

	@Inject
	FeedRefreshWorker feedRefreshWorker;

	@Inject
	FeedRefreshUpdater feedRefreshUpdater;

	@Inject
	FeedRefreshTaskGiver taskGiver;

	@Inject
	PasswordEncryptionService encryptionService;

	@Path("/user/save")
	@POST
	@ApiOperation(value = "Save or update a user", notes = "Save or update a user. If the id is not specified, a new user will be created")
	public Response save(@ApiParam(required = true) UserModel userModel) {
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
				userService.register(userModel.getName(),
						userModel.getPassword(), userModel.getEmail(), roles,
						true);
			} catch (Exception e) {
				return Response.status(Status.CONFLICT).entity(e.getMessage())
						.build();
			}
		} else {
			User user = userDAO.findById(id);
			if (StartupBean.USERNAME_ADMIN.equals(user.getName())
					&& !userModel.isEnabled()) {
				return Response.status(Status.FORBIDDEN)
						.entity("You cannot disable the admin user.").build();
			}
			user.setName(userModel.getName());
			if (StringUtils.isNotBlank(userModel.getPassword())) {
				user.setPassword(encryptionService.getEncryptedPassword(
						userModel.getPassword(), user.getSalt()));
			}
			user.setEmail(userModel.getEmail());
			user.setDisabled(!userModel.isEnabled());
			userDAO.saveOrUpdate(user);

			Set<Role> roles = userRoleDAO.findRoles(user);
			if (userModel.isAdmin() && !roles.contains(Role.ADMIN)) {
				userRoleDAO.saveOrUpdate(new UserRole(user, Role.ADMIN));
			} else if (!userModel.isAdmin() && roles.contains(Role.ADMIN)) {
				if (StartupBean.USERNAME_ADMIN.equals(user.getName())) {
					return Response
							.status(Status.FORBIDDEN)
							.entity("You cannot remove the admin role from the admin user.")
							.build();
				}
				for (UserRole userRole : userRoleDAO.findAll(user)) {
					if (userRole.getRole() == Role.ADMIN) {
						userRoleDAO.delete(userRole);
					}
				}
			}

		}
		return Response.ok(Status.OK).entity("OK").build();

	}

	@Path("/user/get/{id}")
	@GET
	@ApiOperation(value = "Get user information", notes = "Get user information", responseClass = "com.commafeed.frontend.model.UserModel")
	public Response getUser(
			@ApiParam(value = "user id", required = true) @PathParam("id") Long id) {
		Preconditions.checkNotNull(id);
		User user = userDAO.findById(id);
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
		return Response.ok(userModel).build();
	}

	@Path("/user/getAll")
	@GET
	@ApiOperation(value = "Get all users", notes = "Get all users", responseClass = "List[com.commafeed.frontend.model.UserModel]")
	public Response getUsers() {
		Map<Long, UserModel> users = Maps.newHashMap();
		for (UserRole role : userRoleDAO.findAll()) {
			User user = role.getUser();
			Long key = user.getId();
			UserModel userModel = users.get(key);
			if (userModel == null) {
				userModel = new UserModel();
				userModel.setId(user.getId());
				userModel.setName(user.getName());
				userModel.setEmail(user.getEmail());
				userModel.setEnabled(!user.isDisabled());
				userModel.setCreated(user.getCreated());
				userModel.setLastLogin(user.getLastLogin());
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
	@ApiOperation(value = "Delete a user", notes = "Delete a user, and all his subscriptions")
	public Response delete(@ApiParam(required = true) IDRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		User user = userDAO.findById(req.getId());
		if (user == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if (StartupBean.USERNAME_ADMIN.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN)
					.entity("You cannot delete the admin user.").build();
		}
		userService.unregister(user);
		return Response.ok().build();
	}

	@Path("/settings")
	@GET
	@ApiOperation(value = "Retrieve application settings", notes = "Retrieve application settings", responseClass = "com.commafeed.backend.model.ApplicationSettings")
	public Response getSettings() {
		return Response.ok(applicationSettingsService.get()).build();
	}

	@Path("/settings")
	@POST
	@ApiOperation(value = "Save application settings", notes = "Save application settings")
	public Response saveSettings(
			@ApiParam(required = true) ApplicationSettings settings) {
		Preconditions.checkNotNull(settings);
		applicationSettingsService.save(settings);
		return Response.ok().build();
	}

	@Path("/metrics")
	@GET
	@ApiOperation(value = "Retrieve server metrics")
	public Response getMetrics(
			@QueryParam("backlog") @DefaultValue("false") boolean backlog) {
		Map<String, Object> map = Maps.newLinkedHashMap();
		map.put("lastMinute", metricsBean.getLastMinute());
		map.put("lastHour", metricsBean.getLastHour());
		if (backlog) {
			map.put("backlog", taskGiver.getUpdatableCount());
		}
		map.put("http_active", feedRefreshWorker.getActiveCount());
		map.put("http_queue", feedRefreshWorker.getQueueSize());
		map.put("database_active", feedRefreshUpdater.getActiveCount());
		map.put("database_queue", feedRefreshUpdater.getQueueSize());
		map.put("cache", metricsBean.getCacheStats());

		return Response.ok(map).build();
	}

	@Path("/cleanup/feeds")
	@GET
	@ApiOperation(value = "Feeds cleanup", notes = "Delete feeds without subscriptions and entries without feeds")
	public Response cleanupFeeds() {
		Map<String, Long> map = Maps.newHashMap();
		map.put("feeds_without_subscriptions",
				cleaner.cleanFeedsWithoutSubscriptions());
		map.put("entries_without_feeds", cleaner.cleanEntriesWithoutFeeds());
		return Response.ok(map).build();
	}

	@Path("/cleanup/entries")
	@GET
	@ApiOperation(value = "Entries cleanup", notes = "Delete entries older than given date")
	public Response cleanupEntries(
			@QueryParam("days") @DefaultValue("30") int days) {
		Map<String, Long> map = Maps.newHashMap();
		map.put("old entries",
				cleaner.cleanEntriesOlderThan(days, TimeUnit.DAYS));
		return Response.ok(map).build();
	}

	@Path("/cleanup/findDuplicateFeeds")
	@GET
	@ApiOperation(value = "Find duplicate feeds")
	public Response findDuplicateFeeds(@QueryParam("mode") DuplicateMode mode,
			@QueryParam("page") int page, @QueryParam("limit") int limit,
			@QueryParam("minCount") long minCount) {
		List<FeedCount> list = feedDAO.findDuplicates(mode, limit * page,
				limit, minCount);
		return Response.ok(list).build();
	}

	@Path("/cleanup/merge")
	@POST
	@ApiOperation(value = "Merge feeds", notes = "Merge feeds together")
	public Response mergeFeeds(
			@ApiParam(required = true) FeedMergeRequest request) {
		Feed into = feedDAO.findById(request.getIntoFeedId());
		if (into == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("'into feed' not found").build();
		}

		List<Feed> feeds = Lists.newArrayList();
		for (Long feedId : request.getFeedIds()) {
			Feed feed = feedDAO.findById(feedId);
			feeds.add(feed);
		}

		if (feeds.isEmpty()) {
			return Response.status(Status.BAD_REQUEST)
					.entity("'from feeds' empty").build();
		}

		cleaner.mergeFeeds(into, feeds);
		return Response.ok().build();
	}
}
