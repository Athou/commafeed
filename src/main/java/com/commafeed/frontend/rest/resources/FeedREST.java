package com.commafeed.frontend.rest.resources;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.feeds.FetchedFeed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.SecurityCheck;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.FeedInfo;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.request.FeedModificationRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.SubscribeRequest;
import com.commafeed.frontend.rest.Enums.ReadType;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.WireFeedOutput;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/feed")
@Api(value = "/feed", description = "Operations about feeds")
public class FeedREST extends AbstractResourceREST {

	private static Logger log = LoggerFactory.getLogger(FeedREST.class);

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

	@Path("/entriesAsFeed")
	@GET
	@ApiOperation(value = "Get feed entries as a feed", notes = "Get a feed of feed entries")
	@Produces(MediaType.APPLICATION_XML)
	@SecurityCheck(value = Role.USER, apiKeyAllowed = true)
	public String getFeedEntriesAsFeed(
			@ApiParam(value = "id of the feed", required = true) @QueryParam("id") String id) {

		Preconditions.checkNotNull(id);

		ReadType readType = ReadType.all;
		ReadingOrder order = ReadingOrder.desc;
		int offset = 0;
		int limit = 20;

		Entries entries = getFeedEntries(id, readType, offset, limit, order);

		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setTitle("CommaFeed - " + entries.getName());
		feed.setDescription("CommaFeed - " + entries.getName());
		String publicUrl = applicationSettingsService.get().getPublicUrl();
		feed.setLink(publicUrl);

		List<SyndEntry> children = Lists.newArrayList();
		for (Entry entry : entries.getEntries()) {
			children.add(entry.asRss());
		}
		feed.setEntries(children);

		SyndFeedOutput output = new SyndFeedOutput();
		StringWriter writer = new StringWriter();
		try {
			output.output(feed, writer);
		} catch (Exception e) {
			writer.write("Could not get feed information");
			log.error(e.getMessage(), e);
		}
		return writer.toString();
	}

	@GET
	@Path("/fetch")
	@ApiOperation(value = "Fetch a feed", notes = "Fetch a feed by its url", responseClass = "com.commafeed.frontend.model.FeedInfo")
	public FeedInfo fetchFeed(
			@ApiParam(value = "the feed's url", required = true) @QueryParam("url") String url) {
		Preconditions.checkNotNull(url);

		FeedInfo info = null;
		url = StringUtils.trimToEmpty(url);
		url = prependHttp(url);
		try {
			FetchedFeed feed = feedFetcher.fetch(url, true, null, null);
			info = new FeedInfo();
			info.setUrl(feed.getFeed().getUrl());
			info.setTitle(feed.getTitle());

		} catch (Exception e) {
			throw new WebApplicationException(e, Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build());
		}
		return info;
	}

	@Path("/refresh")
	@POST
	@ApiOperation(value = "Queue a feed for refresh", notes = "Manually add a feed to the refresh queue")
	public Response queueForRefresh(@ApiParam(value = "Feed id") IDRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		FeedSubscription sub = feedSubscriptionDAO.findById(getUser(),
				req.getId());
		if (sub != null) {
			taskGiver.add(sub.getFeed());
			return Response.ok(Status.OK).build();
		}
		return Response.ok(Status.NOT_FOUND).build();
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

	@GET
	@Path("/get/{id}")
	@ApiOperation(value = "", notes = "")
	public Subscription get(
			@ApiParam(value = "user id", required = true) @PathParam("id") Long id) {

		Preconditions.checkNotNull(id);
		FeedSubscription sub = feedSubscriptionDAO.findById(getUser(), id);
		return Subscription.build(sub, 0);
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
		FeedInfo info = fetchFeed(url);
		feedSubscriptionService.subscribe(getUser(), info.getUrl(),
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
	public Response unsubscribe(@ApiParam(required = true) IDRequest req) {
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
	@Path("/modify")
	@ApiOperation(value = "Modify a subscription", notes = "Modify a feed subscription")
	public Response modify(
			@ApiParam(value = "subscription id", required = true) FeedModificationRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());
		Preconditions.checkNotNull(req.getName());

		FeedSubscription subscription = feedSubscriptionDAO.findById(getUser(),
				req.getId());
		subscription.setTitle(req.getName());

		FeedCategory parent = null;
		if (req.getCategoryId() != null
				&& !CategoryREST.ALL.equals(req.getCategoryId())) {
			parent = feedCategoryDAO.findById(getUser(),
					Long.valueOf(req.getCategoryId()));
		}
		subscription.setCategory(parent);
		feedSubscriptionDAO.update(subscription);

		return Response.ok(Status.OK).build();
	}

	@POST
	@Path("/import")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "OPML import", notes = "Import an OPML file, posted as a FORM with the 'file' name")
	public Response importOpml() {
		try {
			FileItemFactory factory = new DiskFileItemFactory(1000000, null);
			ServletFileUpload upload = new ServletFileUpload(factory);
			for (FileItem item : upload.parseRequest(request)) {
				if ("file".equals(item.getFieldName())) {
					String opml = IOUtils.toString(item.getInputStream(),
							"UTF-8");
					if (StringUtils.isNotBlank(opml)) {
						opmlImporter.importOpml(getUser(), opml);
					}
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

	@GET
	@Path("/export")
	@Produces(MediaType.APPLICATION_XML)
	@ApiOperation(value = "OPML export", notes = "Export an OPML file of the user's subscriptions")
	public Response exportOpml() {
		Opml opml = opmlExporter.export(getUser());
		WireFeedOutput output = new WireFeedOutput();
		String opmlString = null;
		try {
			opmlString = output.outputString(opml);
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e)
					.build();
		}
		return Response.ok(opmlString).build();
	}

}
