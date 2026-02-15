package com.commafeed.frontend.resource;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.Cache;
import org.jboss.resteasy.reactive.RestForm;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConstants;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.favicon.Favicon;
import com.commafeed.backend.feed.FeedEntryKeyword;
import com.commafeed.backend.feed.FeedFetcher;
import com.commafeed.backend.feed.FeedFetcher.FeedFetcherResult;
import com.commafeed.backend.feed.FeedRefreshEngine;
import com.commafeed.backend.feed.FeedUtils;
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
import com.commafeed.backend.service.FeedSubscriptionService.ForceFeedRefreshTooSoonException;
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
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.WireFeedOutput;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/rest/feed")
@RolesAllowed(Roles.USER)
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
@Tag(name = "Feeds")
public class FeedREST {

	private static final FeedEntry TEST_ENTRY = initTestEntry();

	private final AuthenticationContext authenticationContext;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedCategoryDAO feedCategoryDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedFetcher feedFetcher;
	private final FeedService feedService;
	private final FeedEntryService feedEntryService;
	private final FeedSubscriptionService feedSubscriptionService;
	private final FeedEntryFilteringService feedEntryFilteringService;
	private final FeedRefreshEngine feedRefreshEngine;
	private final OPMLImporter opmlImporter;
	private final OPMLExporter opmlExporter;
	private final CommaFeedConfiguration config;
	private final UriInfo uri;

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

	@Path("/entries")
	@GET
	@Transactional
	@Operation(summary = "Get feed entries", description = "Get a list of feed entries")
	@APIResponse(
			responseCode = "200",
			content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Entries.class)) })
	@APIResponse(responseCode = "404", description = "feed not found")
	public Response getFeedEntries(@Parameter(description = "id of the feed", required = true) @QueryParam("id") String id,
			@Parameter(
					description = "all entries or only unread ones",
					required = true) @DefaultValue("unread") @QueryParam("readType") ReadingMode readType,
			@Parameter(description = "only entries newer than this") @QueryParam("newerThan") Long newerThan,
			@Parameter(description = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@Parameter(description = "limit for paging, default 20, maximum 1000") @DefaultValue("20") @QueryParam("limit") int limit,
			@Parameter(description = "ordering") @QueryParam("order") @DefaultValue("desc") ReadingOrder order, @Parameter(
					description = "search for keywords in either the title or the content of the entries, separated by spaces") @QueryParam("keywords") String keywords) {

		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(readType);

		List<FeedEntryKeyword> entryKeywords = FeedEntryKeyword.fromQueryString(StringUtils.trimToNull(keywords));

		limit = Math.min(limit, 1000);
		limit = Math.max(0, limit);

		Entries entries = new Entries();
		entries.setOffset(offset);
		entries.setLimit(limit);

		boolean unreadOnly = readType == ReadingMode.UNREAD;

		Instant newerThanDate = newerThan == null ? null : Instant.ofEpochMilli(newerThan);

		User user = authenticationContext.getCurrentUser();
		FeedSubscription subscription = feedSubscriptionDAO.findById(user, Long.valueOf(id));
		if (subscription != null) {
			entries.setName(subscription.getTitle());
			entries.setMessage(subscription.getFeed().getMessage());
			entries.setErrorCount(subscription.getFeed().getErrorCount());
			entries.setFeedLink(subscription.getFeed().getLink());

			List<FeedEntryStatus> list = feedEntryStatusDAO.findBySubscriptions(user, Collections.singletonList(subscription), unreadOnly,
					entryKeywords, newerThanDate, offset, limit + 1, order, true, null, null, null);

			for (FeedEntryStatus status : list) {
				entries.getEntries().add(Entry.build(status, config.imageProxyEnabled()));
			}

			boolean hasMore = entries.getEntries().size() > limit;
			if (hasMore) {
				entries.setHasMore(true);
				entries.getEntries().removeLast();
			}
		} else {
			return Response.status(Status.NOT_FOUND).entity("<message>feed not found</message>").build();
		}

		entries.setTimestamp(System.currentTimeMillis());
		entries.setIgnoredReadStatus(keywords != null);
		return Response.ok(entries).build();
	}

	@Path("/entriesAsFeed")
	@GET
	@Transactional
	@Operation(summary = "Get feed entries as a feed", description = "Get a feed of feed entries")
	@Produces(MediaType.APPLICATION_XML)
	public Response getFeedEntriesAsFeed(@Parameter(description = "id of the feed", required = true) @QueryParam("id") String id,
			@Parameter(
					description = "all entries or only unread ones",
					required = true) @DefaultValue("all") @QueryParam("readType") ReadingMode readType,
			@Parameter(description = "only entries newer than this") @QueryParam("newerThan") Long newerThan,
			@Parameter(description = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@Parameter(description = "limit for paging, default 20, maximum 1000") @DefaultValue("20") @QueryParam("limit") int limit,
			@Parameter(description = "date ordering") @QueryParam("order") @DefaultValue("desc") ReadingOrder order, @Parameter(
					description = "search for keywords in either the title or the content of the entries, separated by spaces") @QueryParam("keywords") String keywords) {

		Response response = getFeedEntries(id, readType, newerThan, offset, limit, order, keywords);
		if (response.getStatus() != Status.OK.getStatusCode()) {
			return response;
		}
		Entries entries = (Entries) response.getEntity();

		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setTitle("CommaFeed - " + entries.getName());
		feed.setDescription("CommaFeed - " + entries.getName());
		feed.setLink(uri.getBaseUri().toString());
		feed.setEntries(entries.getEntries().stream().map(FeedUtils::asRss).toList());

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
		FeedInfo info;
		url = StringUtils.trimToEmpty(url);
		url = prependHttp(url);
		try {
			FeedFetcherResult feedFetcherResult = feedFetcher.fetch(url, true, null, null, null, null);
			info = new FeedInfo();
			info.setUrl(feedFetcherResult.urlAfterRedirect());
			info.setTitle(feedFetcherResult.feed().title());

		} catch (Exception e) {
			log.debug(e.getMessage(), e);
			throw new WebApplicationException(e.getMessage(), Status.INTERNAL_SERVER_ERROR);
		}
		return info;
	}

	@POST
	@Path("/fetch")
	@Transactional
	@Operation(summary = "Fetch a feed", description = "Fetch a feed by its url")
	@APIResponse(
			responseCode = "200",
			content = { @Content(mediaType = "application/json", schema = @Schema(implementation = FeedInfo.class)) })
	@APIResponse(responseCode = "404", description = "feed not found")
	public Response fetchFeed(@Valid @Parameter(description = "feed url", required = true) FeedInfoRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getUrl());

		FeedInfo info;
		try {
			info = fetchFeedInternal(req.getUrl());
		} catch (Exception e) {
			Throwable cause = Throwables.getRootCause(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(cause.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
		return Response.ok(info).build();
	}

	@Path("/refreshAll")
	@GET
	@Transactional
	@Operation(summary = "Queue all feeds of the user for refresh", description = "Manually add all feeds of the user to the refresh queue")
	public Response queueAllForRefresh() {
		User user = authenticationContext.getCurrentUser();
		try {
			feedSubscriptionService.refreshAll(user);
			return Response.ok().build();
		} catch (ForceFeedRefreshTooSoonException e) {
			return Response.status(HttpStatus.SC_TOO_MANY_REQUESTS).build();
		}

	}

	@Path("/mark")
	@POST
	@Transactional
	@Operation(summary = "Mark feed entries", description = "Mark feed entries as read (unread is not supported)")
	public Response markFeedEntries(@Valid @Parameter(description = "Mark request", required = true) MarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		Instant olderThan = req.getOlderThan() == null ? null : Instant.ofEpochMilli(req.getOlderThan());
		Instant insertedBefore = req.getInsertedBefore() == null ? null : Instant.ofEpochMilli(req.getInsertedBefore());
		String keywords = req.getKeywords();
		List<FeedEntryKeyword> entryKeywords = FeedEntryKeyword.fromQueryString(keywords);

		User user = authenticationContext.getCurrentUser();
		FeedSubscription subscription = feedSubscriptionDAO.findById(user, Long.valueOf(req.getId()));
		if (subscription != null) {
			feedEntryService.markSubscriptionEntries(user, Collections.singletonList(subscription), olderThan, insertedBefore,
					entryKeywords);
		}
		return Response.ok().build();
	}

	@GET
	@Path("/get/{id}")
	@Transactional
	@Operation(summary = "get feed")
	@APIResponse(
			responseCode = "200",
			content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Subscription.class)) })
	@APIResponse(responseCode = "404", description = "feed not found")
	public Response getFeed(@Parameter(description = "user id", required = true) @PathParam("id") Long id) {
		Preconditions.checkNotNull(id);

		User user = authenticationContext.getCurrentUser();
		FeedSubscription sub = feedSubscriptionDAO.findById(user, id);
		if (sub == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		UnreadCount unreadCount = feedSubscriptionService.getUnreadCount(user).get(id);
		return Response.ok(Subscription.build(sub, unreadCount)).build();
	}

	@GET
	@Path("/favicon/{id}")
	@Cache(maxAge = 2592000)
	@Operation(summary = "Fetch a feed's icon", description = "Fetch a feed's icon")
	public Response getFeedFavicon(@Parameter(description = "subscription id", required = true) @PathParam("id") Long id) {
		Preconditions.checkNotNull(id);

		User user = authenticationContext.getCurrentUser();
		FeedSubscription subscription = feedSubscriptionDAO.findById(user, id);
		if (subscription == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		Feed feed = subscription.getFeed();
		Favicon icon = feedService.fetchFavicon(feed);
		return Response.ok(icon.icon(), icon.mediaType()).build();
	}

	@POST
	@Path("/subscribe")
	@Transactional
	@Operation(summary = "Subscribe to a feed", description = "Subscribe to a feed")
	@APIResponse(
			responseCode = "200",
			content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)) })
	public Response subscribe(@Valid @Parameter(description = "subscription request", required = true) SubscribeRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getTitle());
		Preconditions.checkNotNull(req.getUrl());

		try {
			FeedCategory category = null;
			if (req.getCategoryId() != null && !CategoryREST.ALL.equals(req.getCategoryId())) {
				category = feedCategoryDAO.findById(Long.valueOf(req.getCategoryId()));
			}

			FeedInfo info = fetchFeedInternal(prependHttp(req.getUrl()));
			User user = authenticationContext.getCurrentUser();
			long subscriptionId = feedSubscriptionService.subscribe(user, info.getUrl(), req.getTitle(), category, 0,
					req.isNotifyOnNewEntries());
			return Response.ok(subscriptionId).build();
		} catch (Exception e) {
			log.error("Failed to subscribe to URL {}: {}", req.getUrl(), e.getMessage(), e);
			return Response.status(Status.SERVICE_UNAVAILABLE)
					.entity("Failed to subscribe to URL " + req.getUrl() + ": " + e.getMessage())
					.build();
		}
	}

	@GET
	@Path("/subscribe")
	@Transactional
	@Operation(summary = "Subscribe to a feed", description = "Subscribe to a feed")
	public Response subscribeFromUrl(@Parameter(description = "feed url", required = true) @QueryParam("url") String url) {
		try {
			Preconditions.checkNotNull(url);
			FeedInfo info = fetchFeedInternal(prependHttp(url));
			User user = authenticationContext.getCurrentUser();
			feedSubscriptionService.subscribe(user, info.getUrl(), info.getTitle(), null, 0, true);
		} catch (Exception e) {
			log.info("Could not subscribe to url {} : {}", url, e.getMessage());
		}
		return Response.temporaryRedirect(uri.getBaseUri()).build();
	}

	private String prependHttp(String url) {
		if (!url.startsWith("http")) {
			url = "http://" + url;
		}
		return url;
	}

	@POST
	@Path("/unsubscribe")
	@Transactional
	@Operation(summary = "Unsubscribe from a feed", description = "Unsubscribe from a feed")
	public Response unsubscribe(@Parameter(required = true) IDRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		User user = authenticationContext.getCurrentUser();
		boolean deleted = feedSubscriptionService.unsubscribe(user, req.getId());
		if (deleted) {
			return Response.ok().build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@POST
	@Path("/modify")
	@Transactional
	@Operation(summary = "Modify a subscription", description = "Modify a feed subscription")
	public Response modifyFeed(@Valid @Parameter(description = "subscription id", required = true) FeedModificationRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		try {
			feedEntryFilteringService.filterMatchesEntry(req.getFilter(), TEST_ENTRY);
		} catch (FeedEntryFilterException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getCause().getMessage()).type(MediaType.TEXT_PLAIN).build();
		}

		User user = authenticationContext.getCurrentUser();
		FeedSubscription subscription = feedSubscriptionDAO.findById(user, req.getId());

		subscription.setFilter(req.getFilter());
		if (StringUtils.isNotBlank(subscription.getFilter())) {
			// if the new filter is filled, remove the legacy filter
			subscription.setFilterLegacy(null);
		}

		if (req.getNotifyOnNewEntries() != null) {
			subscription.setNotifyOnNewEntries(req.getNotifyOnNewEntries());
		}

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
			subs.sort((o1, o2) -> ObjectUtils.compare(o1.getPosition(), o2.getPosition()));

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
		}

		return Response.ok().build();
	}

	@POST
	@Path("/import")
	@Transactional
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Operation(summary = "OPML import", description = "Import an OPML file, posted as a FORM with the 'file' name")
	public Response importOpml(@Parameter(description = "ompl file", required = true) @RestForm("file") String opml) {
		User user = authenticationContext.getCurrentUser();
		if (CommaFeedConstants.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).entity("Import is disabled for the demo account").build();
		}
		try {
			// opml will be encoded in the default JVM encoding, bu we want UTF-8
			opmlImporter.importOpml(user, new String(opml.getBytes(SystemUtils.FILE_ENCODING), StandardCharsets.UTF_8));
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		return Response.ok().build();
	}

	@GET
	@Path("/export")
	@Transactional
	@Produces(MediaType.APPLICATION_XML)
	@Operation(summary = "OPML export", description = "Export an OPML file of the user's subscriptions")
	public Response exportOpml() throws FeedException {
		User user = authenticationContext.getCurrentUser();
		Opml opml = opmlExporter.export(user);

		WireFeedOutput output = new WireFeedOutput();
		String opmlString = output.outputString(opml);
		return Response.ok(opmlString).build();
	}

}
