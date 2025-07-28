package com.commafeed.frontend.resource;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.commafeed.backend.dao.FeedEntryTagDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedEntryTagService;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.MultipleMarkRequest;
import com.commafeed.frontend.model.request.StarRequest;
import com.commafeed.frontend.model.request.TagRequest;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;

@Path("/rest/entry")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
@Tag(name = "Feed entries")
public class EntryREST {

	private final AuthenticationContext authenticationContext;
	private final FeedEntryTagDAO feedEntryTagDAO;
	private final FeedEntryService feedEntryService;
	private final FeedEntryTagService feedEntryTagService;

	@Path("/mark")
	@POST
	@Transactional
	@Operation(summary = "Mark a feed entry", description = "Mark a feed entry as read/unread")
	public Response markEntry(@Valid @Parameter(description = "Mark Request", required = true) MarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		User user = authenticationContext.getCurrentUser();
		feedEntryService.markEntry(user, Long.valueOf(req.getId()), req.isRead());
		return Response.ok().build();
	}

	@Path("/markMultiple")
	@POST
	@Transactional
	@Operation(summary = "Mark multiple feed entries", description = "Mark feed entries as read/unread")
	public Response markEntries(@Valid @Parameter(description = "Multiple Mark Request", required = true) MultipleMarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getRequests());

		User user = authenticationContext.getCurrentUser();
		for (MarkRequest r : req.getRequests()) {
			Preconditions.checkNotNull(r.getId());
			feedEntryService.markEntry(user, Long.valueOf(r.getId()), r.isRead());
		}

		return Response.ok().build();
	}

	@Path("/star")
	@POST
	@Transactional
	@Operation(summary = "Star a feed entry", description = "Mark a feed entry as read/unread")
	public Response starEntry(@Valid @Parameter(description = "Star Request", required = true) StarRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());
		Preconditions.checkNotNull(req.getFeedId());

		User user = authenticationContext.getCurrentUser();
		feedEntryService.starEntry(user, Long.valueOf(req.getId()), req.getFeedId(), req.isStarred());

		return Response.ok().build();
	}

	@Path("/tags")
	@GET
	@Transactional
	@Operation(summary = "Get list of tags for the user", description = "Get list of tags for the user")
	public Response getTags() {
		User user = authenticationContext.getCurrentUser();
		List<String> tags = feedEntryTagDAO.findByUser(user);
		return Response.ok(tags).build();
	}

	@Path("/tag")
	@POST
	@Transactional
	@Operation(summary = "Set feed entry tags")
	public Response tagEntry(@Valid @Parameter(description = "Tag Request", required = true) TagRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getEntryId());

		User user = authenticationContext.getCurrentUser();
		feedEntryTagService.updateTags(user, req.getEntryId(), req.getTags());

		return Response.ok().build();
	}

}
