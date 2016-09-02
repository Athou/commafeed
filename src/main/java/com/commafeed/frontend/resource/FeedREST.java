package com.commafeed.frontend.resource;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.codahale.metrics.annotation.Timed;
import com.commafeed.CommaFeedApplication;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.favicon.AbstractFaviconFetcher.Favicon;
import com.commafeed.backend.feed.FeedEntryKeyword;
import com.commafeed.backend.feed.FeedFetcher;
import com.commafeed.backend.feed.FeedQueues;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.feed.FetchedFeed;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.opml.OPMLExporter;
import com.commafeed.backend.opml.OPMLImporter;
import com.commafeed.backend.service.FeedEntryFilteringService;
import com.commafeed.backend.service.FeedEntryFilteringService.FeedEntryFilterException;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedService;
import com.commafeed.backend.service.FeedSubscriptionService;
import com.commafeed.frontend.auth.SecurityCheck;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.FeedInfo;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.UnreadCount;
import com.commafeed.frontend.model.request.FeedInfoRequest;
import com.commafeed.frontend.model.request.FeedModificationRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.SubscribeRequest;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.WireFeedOutput;

import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/feed")
@Api(value = "/feed")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor = @__({ @Inject }) )
@Singleton
public class FeedREST {

	private static final FeedEntry TEST_ENTRY = initTestEntry();

	private static FeedEntry initTestEntry() {
		FeedEntry entry = new FeedEntry();
		entry.setUrl("https://github.com/Athou/commafeed");

		FeedEntryContent content = new FeedEntryContent();
		content.setAuthor("Athou");
		content.setTitle("Merge pull request #662 from Athou/dw8");
		content.setContent("Merge pull request #662 from Athou/dw8");
		entry.setContent(content);
		return entry;
	}

	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedCategoryDAO feedCategoryDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedFetcher feedFetcher;
	private final FeedService feedService;
	private final FeedEntryService feedEntryService;
	private final FeedSubscriptionService feedSubscriptionService;
	private final FeedEntryFilteringService feedEntryFilteringService;
	private final FeedQueues queues;
	private final OPMLImporter opmlImporter;
	private final OPMLExporter opmlExporter;
	private final CacheService cache;
	private final CommaFeedConfiguration config;

	@Path("/entries")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Get feed entries", notes = "Get a list of feed entries", response = Entries.class)
	@Timed
	public Response getFeedEntries(@SecurityCheck User user,
			@ApiParam(value = "id of the feed", required = true) @QueryParam("id") String id,
			@ApiParam(
					value = "all entries or only unread ones",
					allowableValues = "all,unread",
					required = true) @DefaultValue("unread") @QueryParam("readType") ReadingMode readType,
			@ApiParam(value = "only entries newer than this") @QueryParam("newerThan") Long newerThan,
			@ApiParam(value = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@ApiParam(value = "limit for paging, default 20, maximum 1000") @DefaultValue("20") @QueryParam("limit") int limit,
			@ApiParam(value = "ordering", allowableValues = "asc,desc,abc,zyx") @QueryParam("order") @DefaultValue("desc") ReadingOrder order,
			@ApiParam(
					value = "search for keywords in either the title or the content of the entries, separated by spaces, 3 characters minimum") @QueryParam("keywords") String keywords,
			@ApiParam(value = "return only entry ids") @DefaultValue("false") @QueryParam("onlyIds") boolean onlyIds) {

		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(readType);

		keywords = StringUtils.trimToNull(keywords);
		Preconditions.checkArgument(keywords == null || StringUtils.length(keywords) >= 3);
		List<FeedEntryKeyword> entryKeywords = FeedEntryKeyword.fromQueryString(keywords);

		limit = Math.min(limit, 1000);
		limit = Math.max(0, limit);

		Entries entries = new Entries();
		entries.setOffset(offset);
		entries.setLimit(limit);

		boolean unreadOnly = readType == ReadingMode.unread;

		Date newerThanDate = newerThan == null ? null : new Date(newerThan);

		FeedSubscription subscription = feedSubscriptionDAO.findById(user, Long.valueOf(id));
		if (subscription != null) {
			entries.setName(subscription.getTitle());
			entries.setMessage(subscription.getFeed().getMessage());
			entries.setErrorCount(subscription.getFeed().getErrorCount());
			entries.setFeedLink(subscription.getFeed().getLink());

			List<FeedEntryStatus> list = feedEntryStatusDAO.findBySubscriptions(user, Arrays.asList(subscription), unreadOnly,
					entryKeywords, newerThanDate, offset, limit + 1, order, true, onlyIds, null);

			for (FeedEntryStatus status : list) {
				entries.getEntries().add(Entry.build(status, config.getApplicationSettings().getPublicUrl(),
						config.getApplicationSettings().getImageProxyEnabled()));
			}

			boolean hasMore = entries.getEntries().size() > limit;
			if (hasMore) {
				entries.setHasMore(true);
				entries.getEntries().remove(entries.getEntries().size() - 1);
			}
		} else {
			return Response.status(Status.NOT_FOUND).entity("<message>feed not found</message>").build();
		}

		entries.setTimestamp(System.currentTimeMillis());
		entries.setIgnoredReadStatus(keywords != null);
		FeedUtils.removeUnwantedFromSearch(entries.getEntries(), entryKeywords);
		return Response.ok(entries).build();
	}

	@Path("/entriesAsFeed")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Get feed entries as a feed", notes = "Get a feed of feed entries")
	@Produces(MediaType.APPLICATION_XML)
	@Timed
	public Response getFeedEntriesAsFeed(@SecurityCheck(apiKeyAllowed = true) User user,
			@ApiParam(value = "id of the feed", required = true) @QueryParam("id") String id,
			@ApiParam(
					value = "all entries or only unread ones",
					allowableValues = "all,unread",
					required = true) @DefaultValue("all") @QueryParam("readType") ReadingMode readType,
			@ApiParam(value = "only entries newer than this") @QueryParam("newerThan") Long newerThan,
			@ApiParam(value = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@ApiParam(value = "limit for paging, default 20, maximum 1000") @DefaultValue("20") @QueryParam("limit") int limit,
			@ApiParam(value = "date ordering", allowableValues = "asc,desc") @QueryParam("order") @DefaultValue("desc") ReadingOrder order,
			@ApiParam(
					value = "search for keywords in either the title or the content of the entries, separated by spaces, 3 characters minimum") @QueryParam("keywords") String keywords,
			@ApiParam(value = "return only entry ids") @DefaultValue("false") @QueryParam("onlyIds") boolean onlyIds) {

		Response response = getFeedEntries(user, id, readType, newerThan, offset, limit, order, keywords, onlyIds);
		if (response.getStatus() != Status.OK.getStatusCode()) {
			return response;
		}
		Entries entries = (Entries) response.getEntity();

		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setTitle("CommaFeed - " + entries.getName());
		feed.setDescription("CommaFeed - " + entries.getName());
		feed.setLink(config.getApplicationSettings().getPublicUrl());
		feed.setEntries(entries.getEntries().stream().map(e -> e.asRss()).collect(Collectors.toList()));

		SyndFeedOutput output = new SyndFeedOutput();
		StringWriter writer = new StringWriter();
		try {
			output.output(feed, writer);
		} catch (Exception e) {
			writer.write("Could not get feed information");
			log.error(e.getMessage(), e);
		}
		return Response.ok(writer.toString()).build();
	}

	private FeedInfo fetchFeedInternal(String url) {
		FeedInfo info = null;
		url = StringUtils.trimToEmpty(url);
		url = prependHttp(url);
		try {
			FetchedFeed feed = feedFetcher.fetch(url, true, null, null, null, null);
			info = new FeedInfo();
			info.setUrl(feed.getUrlAfterRedirect());
			info.setTitle(feed.getTitle());

		} catch (Exception e) {
			log.debug(e.getMessage(), e);
			throw new WebApplicationException(e, Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
		return info;
	}

	@POST
	@Path("/fetch")
	@UnitOfWork
	@ApiOperation(value = "Fetch a feed", notes = "Fetch a feed by its url", response = FeedInfo.class)
	@Timed
	public Response fetchFeed(@SecurityCheck User user, @ApiParam(value = "feed url", required = true) FeedInfoRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getUrl());

		FeedInfo info = null;
		try {
			info = fetchFeedInternal(req.getUrl());
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(Throwables.getStackTraceAsString(Throwables.getRootCause(e)))
					.type(MediaType.TEXT_PLAIN).build();
		}
		return Response.ok(info).build();
	}

	@Path("/refreshAll")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Queue all feeds of the user for refresh", notes = "Manually add all feeds of the user to the refresh queue")
	@Timed
	public Response queueAllForRefresh(@SecurityCheck User user) {
		feedSubscriptionService.refreshAll(user);
		return Response.ok().build();
	}

	@Path("/refresh")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Queue a feed for refresh", notes = "Manually add a feed to the refresh queue")
	@Timed
	public Response queueForRefresh(@SecurityCheck User user, @ApiParam(value = "Feed id") IDRequest req) {

		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		FeedSubscription sub = feedSubscriptionDAO.findById(user, req.getId());
		if (sub != null) {
			Feed feed = sub.getFeed();
			queues.add(feed, true);
			return Response.ok().build();
		}
		return Response.ok(Status.NOT_FOUND).build();
	}

	@Path("/mark")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Mark feed entries", notes = "Mark feed entries as read (unread is not supported)")
	@Timed
	public Response markFeedEntries(@SecurityCheck User user, @ApiParam(value = "Mark request") MarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		Date olderThan = req.getOlderThan() == null ? null : new Date(req.getOlderThan());
		String keywords = req.getKeywords();
		List<FeedEntryKeyword> entryKeywords = FeedEntryKeyword.fromQueryString(keywords);

		FeedSubscription subscription = feedSubscriptionDAO.findById(user, Long.valueOf(req.getId()));
		if (subscription != null) {
			feedEntryService.markSubscriptionEntries(user, Arrays.asList(subscription), olderThan, entryKeywords);
		}
		return Response.ok().build();
	}

	@GET
	@Path("/get/{id}")
	@UnitOfWork
	@ApiOperation(value = "", notes = "")
	@Timed
	public Response get(@SecurityCheck User user, @ApiParam(value = "user id", required = true) @PathParam("id") Long id) {

		Preconditions.checkNotNull(id);
		FeedSubscription sub = feedSubscriptionDAO.findById(user, id);
		if (sub == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		UnreadCount unreadCount = feedSubscriptionService.getUnreadCount(user).get(id);
		return Response.ok(Subscription.build(sub, config.getApplicationSettings().getPublicUrl(), unreadCount)).build();
	}

	@GET
	@Path("/favicon/{id}")
	@UnitOfWork
	@ApiOperation(value = "Fetch a feed's icon", notes = "Fetch a feed's icon")
	@Timed
	public Response getFavicon(@SecurityCheck User user, @ApiParam(value = "subscription id") @PathParam("id") Long id) {

		Preconditions.checkNotNull(id);
		FeedSubscription subscription = feedSubscriptionDAO.findById(user, id);
		if (subscription == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		Feed feed = subscription.getFeed();
		Favicon icon = feedService.fetchFavicon(feed);

		ResponseBuilder builder = Response.ok(icon.getIcon(), Optional.ofNullable(icon.getMediaType()).orElse("image/x-icon"));

		CacheControl cacheControl = new CacheControl();
		cacheControl.setMaxAge(2592000);
		cacheControl.setPrivate(false);
		builder.cacheControl(cacheControl);

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 1);
		builder.expires(calendar.getTime());
		builder.lastModified(CommaFeedApplication.STARTUP_TIME);

		return builder.build();
	}

	@POST
	@Path("/subscribe")
	@UnitOfWork
	@ApiOperation(value = "Subscribe to a feed", notes = "Subscribe to a feed")
	@Timed
	public Response subscribe(@SecurityCheck User user, @ApiParam(value = "subscription request", required = true) SubscribeRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getTitle());
		Preconditions.checkNotNull(req.getUrl());

		String url = prependHttp(req.getUrl());
		try {
			url = fetchFeedInternal(url).getUrl();

			FeedCategory category = null;
			if (req.getCategoryId() != null && !CategoryREST.ALL.equals(req.getCategoryId())) {
				category = feedCategoryDAO.findById(Long.valueOf(req.getCategoryId()));
			}
			FeedInfo info = fetchFeedInternal(url);
			feedSubscriptionService.subscribe(user, info.getUrl(), req.getTitle(), category);
		} catch (Exception e) {
			log.error("Failed to subscribe to URL {}: {}", url, e.getMessage(), e);
			return Response.status(Status.SERVICE_UNAVAILABLE).entity("Failed to subscribe to URL " + url + ": " + e.getMessage()).build();
		}
		return Response.ok().build();
	}

	@GET
	@Path("/subscribe")
	@UnitOfWork
	@ApiOperation(value = "Subscribe to a feed", notes = "Subscribe to a feed")
	@Timed
	public Response subscribe(@SecurityCheck User user, @ApiParam(value = "feed url", required = true) @QueryParam("url") String url) {

		try {
			Preconditions.checkNotNull(url);

			url = prependHttp(url);
			url = fetchFeedInternal(url).getUrl();

			FeedInfo info = fetchFeedInternal(url);
			feedSubscriptionService.subscribe(user, info.getUrl(), info.getTitle());
		} catch (Exception e) {
			log.info("Could not subscribe to url {} : {}", url, e.getMessage());
		}
		return Response.temporaryRedirect(URI.create(config.getApplicationSettings().getPublicUrl())).build();
	}

	private String prependHttp(String url) {
		if (!url.startsWith("http")) {
			url = "http://" + url;
		}
		return url;
	}

	@POST
	@Path("/unsubscribe")
	@UnitOfWork
	@ApiOperation(value = "Unsubscribe from a feed", notes = "Unsubscribe from a feed")
	@Timed
	public Response unsubscribe(@SecurityCheck User user, @ApiParam(required = true) IDRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		boolean deleted = feedSubscriptionService.unsubscribe(user, req.getId());
		if (deleted) {
			return Response.ok().build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@POST
	@Path("/modify")
	@UnitOfWork
	@ApiOperation(value = "Modify a subscription", notes = "Modify a feed subscription")
	@Timed
	public Response modify(@SecurityCheck User user, @ApiParam(value = "subscription id", required = true) FeedModificationRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		try {
			feedEntryFilteringService.filterMatchesEntry(req.getFilter(), TEST_ENTRY);
		} catch (FeedEntryFilterException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getCause().getMessage()).type(MediaType.TEXT_PLAIN).build();
		}

		FeedSubscription subscription = feedSubscriptionDAO.findById(user, req.getId());
		subscription.setFilter(StringUtils.lowerCase(req.getFilter()));

		if (StringUtils.isNotBlank(req.getName())) {
			subscription.setTitle(req.getName());
		}

		FeedCategory parent = null;
		if (req.getCategoryId() != null && !CategoryREST.ALL.equals(req.getCategoryId())) {
			parent = feedCategoryDAO.findById(user, Long.valueOf(req.getCategoryId()));
		}
		subscription.setCategory(parent);

		if (req.getPosition() != null) {
			List<FeedSubscription> subs = feedSubscriptionDAO.findByCategory(user, parent);
			Collections.sort(subs, new Comparator<FeedSubscription>() {
				@Override
				public int compare(FeedSubscription o1, FeedSubscription o2) {
					return ObjectUtils.compare(o1.getPosition(), o2.getPosition());
				}
			});

			int existingIndex = -1;
			for (int i = 0; i < subs.size(); i++) {
				if (Objects.equals(subs.get(i).getId(), subscription.getId())) {
					existingIndex = i;
				}
			}
			if (existingIndex != -1) {
				subs.remove(existingIndex);
			}

			subs.add(Math.min(req.getPosition(), subs.size()), subscription);
			for (int i = 0; i < subs.size(); i++) {
				subs.get(i).setPosition(i);
			}
			feedSubscriptionDAO.saveOrUpdate(subs);
		} else {
			feedSubscriptionDAO.saveOrUpdate(subscription);
		}
		cache.invalidateUserRootCategory(user);
		return Response.ok().build();
	}

	@POST
	@Path("/import")
	@UnitOfWork
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "OPML import", notes = "Import an OPML file, posted as a FORM with the 'file' name")
	@Timed
	public Response importOpml(@SecurityCheck User user, @FormDataParam("file") InputStream input) {

		String publicUrl = config.getApplicationSettings().getPublicUrl();
		if (StringUtils.isBlank(publicUrl)) {
			throw new WebApplicationException(
					Response.status(Status.INTERNAL_SERVER_ERROR).entity("Set the public URL in the admin section.").build());
		}

		if (CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).entity("Import is disabled for the demo account").build();
		}
		try {
			String opml = IOUtils.toString(input, "UTF-8");
			opmlImporter.importOpml(user, opml);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
		}
		return Response.seeOther(URI.create(config.getApplicationSettings().getPublicUrl())).build();
	}

	@GET
	@Path("/export")
	@UnitOfWork
	@Produces(MediaType.APPLICATION_XML)
	@ApiOperation(value = "OPML export", notes = "Export an OPML file of the user's subscriptions")
	@Timed
	public Response exportOpml(@SecurityCheck User user) {
		Opml opml = opmlExporter.export(user);
		WireFeedOutput output = new WireFeedOutput();
		String opmlString = null;
		try {
			opmlString = output.outputString(opml);
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
		}
		return Response.ok(opmlString).build();
	}

}
