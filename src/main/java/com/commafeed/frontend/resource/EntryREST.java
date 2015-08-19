package com.commafeed.frontend.resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.commafeed.backend.dao.FeedEntryTagDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedEntryTagService;
import com.commafeed.frontend.auth.SecurityCheck;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.MultipleMarkRequest;
import com.commafeed.frontend.model.request.StarRequest;
import com.commafeed.frontend.model.request.TagRequest;
import com.google.common.base.Preconditions;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@Path("/entry")
@Api(value = "/entry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor = @__({ @Inject }) )
@Singleton
public class EntryREST {

	private final FeedEntryTagDAO feedEntryTagDAO;
	private final FeedEntryService feedEntryService;
	private final FeedEntryTagService feedEntryTagService;

	@Path("/mark")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Mark a feed entry", notes = "Mark a feed entry as read/unread")
	@Timed
	public Response markFeedEntry(@SecurityCheck User user, @ApiParam(value = "Mark Request", required = true) MarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		feedEntryService.markEntry(user, Long.valueOf(req.getId()), req.isRead());
		return Response.ok().build();
	}

	@Path("/markMultiple")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Mark multiple feed entries", notes = "Mark feed entries as read/unread")
	@Timed
	public Response markFeedEntries(@SecurityCheck User user,
			@ApiParam(value = "Multiple Mark Request", required = true) MultipleMarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getRequests());

		for (MarkRequest r : req.getRequests()) {
			markFeedEntry(user, r);
		}

		return Response.ok().build();
	}

	@Path("/star")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Mark a feed entry", notes = "Mark a feed entry as read/unread")
	@Timed
	public Response starFeedEntry(@SecurityCheck User user, @ApiParam(value = "Star Request", required = true) StarRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());
		Preconditions.checkNotNull(req.getFeedId());

		feedEntryService.starEntry(user, Long.valueOf(req.getId()), req.getFeedId(), req.isStarred());

		return Response.ok().build();
	}

	@Path("/tags")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Get list of tags for the user", notes = "Get list of tags for the user")
	@Timed
	public Response getTags(@SecurityCheck User user) {
		List<String> tags = feedEntryTagDAO.findByUser(user);
		return Response.ok(tags).build();
	}

	@Path("/tag")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Mark a feed entry", notes = "Mark a feed entry as read/unread")
	@Timed
	public Response tagFeedEntry(@SecurityCheck User user, @ApiParam(value = "Tag Request", required = true) TagRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getEntryId());

		feedEntryTagService.updateTags(user, req.getEntryId(), req.getTags());

		return Response.ok().build();
	}

}
