package com.commafeed.frontend.rest.resources;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.RenameRequest;
import com.commafeed.frontend.model.request.SubscribeRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.rest.Enums.ReadType;
import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/feed")
@Api(value = "/feed", description = "Operations about feeds")
public class FeedREST extends AbstractResourceREST {

	@Path("/entries")
	@GET
	@ApiOperation(value = "Get feed entries", notes = "Get a list of feed entries", responseClass = "com.commafeed.frontend.model.Entries")
	public Entries getFeedEntries(
			@ApiParam(value = "id of the feed", required = true) @QueryParam("id") String id,
			@ApiParam(value = "all entries or only unread ones", allowableValues = "all,unread", required = true) @QueryParam("readType") ReadType readType,
			@ApiParam(value = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@ApiParam(value = "limit for paging") @DefaultValue("-1") @QueryParam("limit") int limit,
			@ApiParam(value = "date ordering", allowableValues = "asc,desc") @QueryParam("order") @DefaultValue("desc") ReadingOrder order) {

		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(readType);

		Entries entries = new Entries();
		boolean unreadOnly = readType == ReadType.unread;

		FeedSubscription subscription = feedSubscriptionDAO.findById(getUser(),
				Long.valueOf(id));
		if (subscription != null) {
			entries.setName(subscription.getTitle());
			entries.setMessage(subscription.getFeed().getMessage());
			entries.setErrorCount(subscription.getFeed().getErrorCount());

			List<FeedEntryStatus> unreadEntries = feedEntryStatusDAO
					.findByFeed(subscription.getFeed(), getUser(), unreadOnly,
							offset, limit, order, true);
			for (FeedEntryStatus status : unreadEntries) {
				entries.getEntries().add(Entry.build(status));
			}
		}

		entries.setTimestamp(Calendar.getInstance().getTimeInMillis());
		return entries;
	}

	@GET
	@Path("/fetch")
	@ApiOperation(value = "Fetch a feed", notes = "Fetch a feed by its url", responseClass = "com.commafeed.backend.model.Feed")
	public Feed fetchFeed(
			@ApiParam(value = "the feed's url", required = true) @QueryParam("url") String url) {
		Preconditions.checkNotNull(url);
		url = StringUtils.trimToEmpty(url);
		url = prependHttp(url);
		Feed feed = null;
		try {
			feed = feedFetcher.fetch(url, true, null, null);
		} catch (Exception e) {
			throw new WebApplicationException(e, Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build());
		}
		return feed;
	}

	@Path("/mark")
	@POST
	@ApiOperation(value = "Mark feed entries", notes = "Mark feed entries as read (unread is not supported)")
	public Response markFeedEntries(
			@ApiParam(value = "Mark request") MarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		Date olderThan = req.getOlderThan() == null ? null : new Date(
				req.getOlderThan());

		FeedSubscription subscription = feedSubscriptionDAO.findById(getUser(),
				Long.valueOf(req.getId()));
		feedEntryStatusDAO.markFeedEntries(getUser(), subscription.getFeed(),
				olderThan);

		return Response.ok(Status.OK).build();
	}

	@POST
	@Path("/subscribe")
	@ApiOperation(value = "Subscribe to a feed", notes = "Subscribe to a feed")
	public Response subscribe(
			@ApiParam(value = "subscription request", required = true) SubscribeRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getTitle());
		Preconditions.checkNotNull(req.getUrl());

		String url = prependHttp(req.getUrl());
		url = fetchFeed(url).getUrl();

		FeedCategory category = CategoryREST.ALL.equals(req.getCategoryId()) ? null
				: feedCategoryDAO.findById(Long.valueOf(req.getCategoryId()));
		Feed fetchedFeed = fetchFeed(url);
		feedSubscriptionService.subscribe(getUser(), fetchedFeed.getUrl(),
				req.getTitle(), category);

		return Response.ok(Status.OK).build();
	}

	private String prependHttp(String url) {
		if (!url.startsWith("http")) {
			url = "http://" + url;
		}
		return url;
	}

	@POST
	@Path("/unsubscribe")
	@ApiOperation(value = "Unsubscribe to a feed", notes = "Unsubscribe to a feed")
	public Response unsubscribe(
			@ApiParam(required = true) IDRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		FeedSubscription sub = feedSubscriptionDAO.findById(getUser(),
				req.getId());
		if (sub != null) {
			feedSubscriptionDAO.delete(sub);
			return Response.ok(Status.OK).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@POST
	@Path("/rename")
	@ApiOperation(value = "Rename a subscription", notes = "Rename a feed subscription")
	public Response rename(
			@ApiParam(value = "subscription id", required = true) RenameRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());
		Preconditions.checkNotNull(req.getName());

		FeedSubscription subscription = feedSubscriptionDAO.findById(getUser(),
				req.getId());
		subscription.setTitle(req.getName());
		feedSubscriptionDAO.update(subscription);

		return Response.ok(Status.OK).build();
	}

	@POST
	@Path("/import")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "OPML Import", notes = "Import an OPML file, posted as a FORM with the 'file' name")
	public Response importOpml() {
		try {
			FileItemFactory factory = new DiskFileItemFactory(1000000, null);
			ServletFileUpload upload = new ServletFileUpload(factory);
			for (FileItem item : upload.parseRequest(request)) {
				if ("file".equals(item.getFieldName())) {
					opmlImporter.importOpml(getUser(),
							IOUtils.toString(item.getInputStream(), "UTF-8"));
					break;
				}
			}
		} catch (Exception e) {
			throw new WebApplicationException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build());
		}
		return Response.ok(Status.OK).build();
	}

}
