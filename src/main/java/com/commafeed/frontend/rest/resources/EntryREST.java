package com.commafeed.frontend.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.FeedEntryService;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.MultipleMarkRequest;
import com.commafeed.frontend.model.request.StarRequest;
import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/entry")
@Api(value = "/entry", description = "Operations about feed entries")
public class EntryREST extends AbstractREST {

	@Inject
	FeedEntryService feedEntryService;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;
	
	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Path("/mark")
	@POST
	@ApiOperation(value = "Mark a feed entry", notes = "Mark a feed entry as read/unread")
	public Response markFeedEntry(@ApiParam(value = "Mark Request", required = true) MarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());
		Preconditions.checkNotNull(req.getFeedId());

		feedEntryService.markEntry(getUser(), Long.valueOf(req.getId()), req.getFeedId(), req.isRead());
		return Response.ok(Status.OK).build();
	}

	@Path("/markMultiple")
	@POST
	@ApiOperation(value = "Mark multiple feed entries", notes = "Mark feed entries as read/unread")
	public Response markFeedEntries(@ApiParam(value = "Multiple Mark Request", required = true) MultipleMarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getRequests());

		for (MarkRequest r : req.getRequests()) {
			markFeedEntry(r);
		}

		return Response.ok(Status.OK).build();
	}

	@Path("/star")
	@POST
	@ApiOperation(value = "Mark a feed entry", notes = "Mark a feed entry as read/unread")
	public Response starFeedEntry(@ApiParam(value = "Star Request", required = true) StarRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());
		Preconditions.checkNotNull(req.getFeedId());

		feedEntryService.starEntry(getUser(), Long.valueOf(req.getId()), req.getFeedId(), req.isStarred());

		return Response.ok(Status.OK).build();
	}



}
