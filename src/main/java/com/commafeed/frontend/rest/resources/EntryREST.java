package com.commafeed.frontend.rest.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.services.FeedEntryService;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.StarRequest;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/entry")
@Api(value = "/entry", description = "Operations about feed entries")
public class EntryREST extends AbstractResourceREST {

	@Inject
	FeedEntryService feedEntryService;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Path("/mark")
	@POST
	@ApiOperation(value = "Mark a feed entry", notes = "Mark a feed entry as read/unread")
	public Response markFeedEntry(
			@ApiParam(value = "Mark Request", required = true) MarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());
		Preconditions.checkNotNull(req.getFeedId());

		feedEntryService.markEntry(getUser(), Long.valueOf(req.getId()),
				req.getFeedId(), req.isRead());

		return Response.ok(Status.OK).build();
	}

	@Path("/star")
	@POST
	@ApiOperation(value = "Mark a feed entry", notes = "Mark a feed entry as read/unread")
	public Response starFeedEntry(
			@ApiParam(value = "Star Request", required = true) StarRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());
		Preconditions.checkNotNull(req.getFeedId());

		feedEntryService.starEntry(getUser(), Long.valueOf(req.getId()),
				req.getFeedId(), req.isStarred());

		return Response.ok(Status.OK).build();
	}

	@Path("/search")
	@GET
	@ApiOperation(value = "Search for entries", notes = "Look through title and content of entries by keywords", responseClass = "com.commafeed.frontend.model.Entries")
	public Response searchEntries(
			@ApiParam(value = "keywords separated by spaces, 3 characters minimum", required = true) @QueryParam("keywords") String keywords,
			@ApiParam(value = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@ApiParam(value = "limit for paging") @DefaultValue("-1") @QueryParam("limit") int limit) {
		keywords = StringUtils.trimToEmpty(keywords);
		limit = Math.min(limit, 50);
		Preconditions.checkArgument(StringUtils.length(keywords) >= 3);

		Entries entries = new Entries();

		List<Entry> list = Lists.newArrayList();
		List<FeedEntryStatus> entriesStatus = feedEntryStatusDAO
				.findByKeywords(getUser(), keywords, offset, limit);
		for (FeedEntryStatus status : entriesStatus) {
			list.add(Entry.build(status, applicationSettingsService.get()
					.getPublicUrl(), applicationSettingsService.get()
					.isImageProxyEnabled()));
		}

		entries.setName("Search for : " + keywords);
		entries.getEntries().addAll(list);
		return Response.ok(entries).build();
	}

}
