package com.commafeed.frontend.resource;

import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feed.FeedEntryKeyword;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedSubscriptionService;
import com.commafeed.frontend.model.Category;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.UnreadCount;
import com.commafeed.frontend.model.request.AddCategoryRequest;
import com.commafeed.frontend.model.request.CategoryModificationRequest;
import com.commafeed.frontend.model.request.CollapseRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("/rest/category")
@RolesAllowed(Roles.USER)
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
@Tag(name = "Feed categories")
public class CategoryREST {

	public static final String ALL = "all";
	public static final String STARRED = "starred";

	private final AuthenticationContext authenticationContext;
	private final FeedCategoryDAO feedCategoryDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedEntryService feedEntryService;
	private final FeedSubscriptionService feedSubscriptionService;
	private final CommaFeedConfiguration config;
	private final UriInfo uri;

	@Path("/entries")
	@GET
	@Transactional
	@Operation(
			summary = "Get category entries",
			description = "Get a list of category entries",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Entries.class))) })
	public Response getCategoryEntries(
			@Parameter(description = "id of the category, 'all' or 'starred'", required = true) @QueryParam("id") String id,
			@Parameter(
					description = "all entries or only unread ones",
					required = true) @DefaultValue("unread") @QueryParam("readType") ReadingMode readType,
			@Parameter(description = "only entries newer than this") @QueryParam("newerThan") Long newerThan,
			@Parameter(description = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@Parameter(description = "limit for paging, default 20, maximum 1000") @DefaultValue("20") @QueryParam("limit") int limit,
			@Parameter(description = "ordering") @QueryParam("order") @DefaultValue("desc") ReadingOrder order,
			@Parameter(
					description = "search for keywords in either the title or the content of the entries, separated by spaces, 3 characters minimum") @QueryParam("keywords") String keywords,
			@Parameter(
					description = "comma-separated list of excluded subscription ids") @QueryParam("excludedSubscriptionIds") String excludedSubscriptionIds,
			@Parameter(description = "keep only entries tagged with this tag") @QueryParam("tag") String tag) {

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
		if (StringUtils.isBlank(id)) {
			id = ALL;
		}

		Instant newerThanDate = newerThan == null ? null : Instant.ofEpochMilli(newerThan);

		List<Long> excludedIds = null;
		if (StringUtils.isNotEmpty(excludedSubscriptionIds)) {
			excludedIds = Arrays.stream(excludedSubscriptionIds.split(",")).map(Long::valueOf).toList();
		}

		User user = authenticationContext.getCurrentUser();
		if (ALL.equals(id)) {
			entries.setName(Optional.ofNullable(tag).orElse("All"));

			List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
			removeExcludedSubscriptions(subs, excludedIds);
			List<FeedEntryStatus> list = feedEntryStatusDAO.findBySubscriptions(user, subs, unreadOnly, entryKeywords, newerThanDate,
					offset, limit + 1, order, true, tag, null, null);

			for (FeedEntryStatus status : list) {
				entries.getEntries().add(Entry.build(status, config.imageProxyEnabled()));
			}

		} else if (STARRED.equals(id)) {
			entries.setName("Starred");
			List<FeedEntryStatus> starred = feedEntryStatusDAO.findStarred(user, newerThanDate, offset, limit + 1, order, true);
			for (FeedEntryStatus status : starred) {
				entries.getEntries().add(Entry.build(status, config.imageProxyEnabled()));
			}
		} else {
			FeedCategory parent = feedCategoryDAO.findById(user, Long.valueOf(id));
			if (parent != null) {
				List<FeedCategory> categories = feedCategoryDAO.findAllChildrenCategories(user, parent);
				List<FeedSubscription> subs = feedSubscriptionDAO.findByCategories(user, categories);
				removeExcludedSubscriptions(subs, excludedIds);
				List<FeedEntryStatus> list = feedEntryStatusDAO.findBySubscriptions(user, subs, unreadOnly, entryKeywords, newerThanDate,
						offset, limit + 1, order, true, tag, null, null);

				for (FeedEntryStatus status : list) {
					entries.getEntries().add(Entry.build(status, config.imageProxyEnabled()));
				}
				entries.setName(parent.getName());
			} else {
				return Response.status(Status.NOT_FOUND).entity("<message>category not found</message>").build();
			}
		}

		boolean hasMore = entries.getEntries().size() > limit;
		if (hasMore) {
			entries.setHasMore(true);
			entries.getEntries().remove(entries.getEntries().size() - 1);
		}

		entries.setTimestamp(System.currentTimeMillis());
		entries.setIgnoredReadStatus(STARRED.equals(id) || keywords != null || tag != null);
		FeedUtils.removeUnwantedFromSearch(entries.getEntries(), entryKeywords);
		return Response.ok(entries).build();
	}

	@Path("/entriesAsFeed")
	@GET
	@Transactional
	@Operation(summary = "Get category entries as feed", description = "Get a feed of category entries")
	@Produces(MediaType.APPLICATION_XML)
	public Response getCategoryEntriesAsFeed(
			@Parameter(description = "id of the category, 'all' or 'starred'", required = true) @QueryParam("id") String id,
			@Parameter(
					description = "all entries or only unread ones",
					required = true) @DefaultValue("all") @QueryParam("readType") ReadingMode readType,
			@Parameter(description = "only entries newer than this") @QueryParam("newerThan") Long newerThan,
			@Parameter(description = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@Parameter(description = "limit for paging, default 20, maximum 1000") @DefaultValue("20") @QueryParam("limit") int limit,
			@Parameter(description = "date ordering") @QueryParam("order") @DefaultValue("desc") ReadingOrder order,
			@Parameter(
					description = "search for keywords in either the title or the content of the entries, separated by spaces, 3 characters minimum") @QueryParam("keywords") String keywords,
			@Parameter(
					description = "comma-separated list of excluded subscription ids") @QueryParam("excludedSubscriptionIds") String excludedSubscriptionIds,
			@Parameter(description = "keep only entries tagged with this tag") @QueryParam("tag") String tag) {

		Response response = getCategoryEntries(id, readType, newerThan, offset, limit, order, keywords, excludedSubscriptionIds, tag);
		if (response.getStatus() != Status.OK.getStatusCode()) {
			return response;
		}
		Entries entries = (Entries) response.getEntity();

		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setTitle("CommaFeed - " + entries.getName());
		feed.setDescription("CommaFeed - " + entries.getName());
		feed.setLink(uri.getBaseUri().toString());
		feed.setEntries(entries.getEntries().stream().map(Entry::asRss).toList());

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

	@Path("/mark")
	@POST
	@Transactional
	@Operation(summary = "Mark category entries", description = "Mark feed entries of this category as read")
	public Response markCategoryEntries(@Valid @Parameter(description = "category id, or 'all'", required = true) MarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		Instant olderThan = req.getOlderThan() == null ? null : Instant.ofEpochMilli(req.getOlderThan());
		Instant insertedBefore = req.getInsertedBefore() == null ? null : Instant.ofEpochMilli(req.getInsertedBefore());
		String keywords = req.getKeywords();
		List<FeedEntryKeyword> entryKeywords = FeedEntryKeyword.fromQueryString(keywords);

		User user = authenticationContext.getCurrentUser();
		if (ALL.equals(req.getId())) {
			List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
			removeExcludedSubscriptions(subs, req.getExcludedSubscriptions());
			feedEntryService.markSubscriptionEntries(user, subs, olderThan, insertedBefore, entryKeywords);
		} else if (STARRED.equals(req.getId())) {
			feedEntryService.markStarredEntries(user, olderThan, insertedBefore);
		} else {
			FeedCategory parent = feedCategoryDAO.findById(user, Long.valueOf(req.getId()));
			List<FeedCategory> categories = feedCategoryDAO.findAllChildrenCategories(user, parent);
			List<FeedSubscription> subs = feedSubscriptionDAO.findByCategories(user, categories);
			removeExcludedSubscriptions(subs, req.getExcludedSubscriptions());
			feedEntryService.markSubscriptionEntries(user, subs, olderThan, insertedBefore, entryKeywords);
		}
		return Response.ok().build();
	}

	private void removeExcludedSubscriptions(List<FeedSubscription> subs, List<Long> excludedIds) {
		if (CollectionUtils.isNotEmpty(excludedIds)) {
			subs.removeIf(sub -> excludedIds.contains(sub.getId()));
		}
	}

	@Path("/add")
	@POST
	@Transactional
	@Operation(
			summary = "Add a category",
			description = "Add a new feed category",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Long.class))) })
	public Response addCategory(@Valid @Parameter(required = true) AddCategoryRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getName());

		User user = authenticationContext.getCurrentUser();

		FeedCategory cat = new FeedCategory();
		cat.setName(req.getName());
		cat.setUser(user);
		cat.setPosition(0);
		String parentId = req.getParentId();
		if (parentId != null && !ALL.equals(parentId)) {
			FeedCategory parent = new FeedCategory();
			parent.setId(Long.valueOf(parentId));
			cat.setParent(parent);
		}
		feedCategoryDAO.persist(cat);
		return Response.ok(cat.getId()).build();
	}

	@POST
	@Path("/delete")
	@Transactional
	@Operation(summary = "Delete a category", description = "Delete an existing feed category")
	public Response deleteCategory(@Parameter(required = true) IDRequest req) {

		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		User user = authenticationContext.getCurrentUser();
		FeedCategory cat = feedCategoryDAO.findById(user, req.getId());
		if (cat != null) {
			List<FeedSubscription> subs = feedSubscriptionDAO.findByCategory(user, cat);
			for (FeedSubscription sub : subs) {
				sub.setCategory(null);
			}

			List<FeedCategory> categories = feedCategoryDAO.findAllChildrenCategories(user, cat);
			for (FeedCategory child : categories) {
				if (!child.getId().equals(cat.getId()) && child.getParent().getId().equals(cat.getId())) {
					child.setParent(null);
				}
			}

			feedCategoryDAO.delete(cat);
			return Response.ok().build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@POST
	@Path("/modify")
	@Transactional
	@Operation(summary = "Modify a category", description = "Modify an existing feed category")
	public Response modifyCategory(@Valid @Parameter(required = true) CategoryModificationRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		User user = authenticationContext.getCurrentUser();
		FeedCategory category = feedCategoryDAO.findById(user, req.getId());

		if (StringUtils.isNotBlank(req.getName())) {
			category.setName(req.getName());
		}

		FeedCategory parent = null;
		if (req.getParentId() != null && !CategoryREST.ALL.equals(req.getParentId())
				&& !StringUtils.equals(req.getParentId(), String.valueOf(req.getId()))) {
			parent = feedCategoryDAO.findById(user, Long.valueOf(req.getParentId()));
		}
		category.setParent(parent);

		if (req.getPosition() != null) {
			List<FeedCategory> categories = feedCategoryDAO.findByParent(user, parent);
			categories.sort((o1, o2) -> ObjectUtils.compare(o1.getPosition(), o2.getPosition()));

			int existingIndex = -1;
			for (int i = 0; i < categories.size(); i++) {
				if (Objects.equals(categories.get(i).getId(), category.getId())) {
					existingIndex = i;
				}
			}
			if (existingIndex != -1) {
				categories.remove(existingIndex);
			}

			categories.add(Math.min(req.getPosition(), categories.size()), category);
			for (int i = 0; i < categories.size(); i++) {
				categories.get(i).setPosition(i);
			}
		}

		return Response.ok().build();
	}

	@POST
	@Path("/collapse")
	@Transactional
	@Operation(summary = "Collapse a category", description = "Save collapsed or expanded status for a category")
	public Response collapseCategory(@Parameter(required = true) CollapseRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		User user = authenticationContext.getCurrentUser();
		FeedCategory category = feedCategoryDAO.findById(user, req.getId());
		if (category == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		category.setCollapsed(req.isCollapse());

		return Response.ok().build();
	}

	@GET
	@Path("/unreadCount")
	@Transactional
	@Operation(
			summary = "Get unread count for feed subscriptions",
			responses = { @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = UnreadCount.class)))) })
	public Response getUnreadCount() {
		User user = authenticationContext.getCurrentUser();
		Map<Long, UnreadCount> unreadCount = feedSubscriptionService.getUnreadCount(user);
		return Response.ok(Lists.newArrayList(unreadCount.values())).build();
	}

	@GET
	@Path("/get")
	@Transactional
	@Operation(
			summary = "Get root category",
			description = "Get all categories and subscriptions of the user",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = Category.class))) })
	public Response getRootCategory() {
		User user = authenticationContext.getCurrentUser();

		List<FeedCategory> categories = feedCategoryDAO.findAll(user);
		List<FeedSubscription> subscriptions = feedSubscriptionDAO.findAll(user);
		Map<Long, UnreadCount> unreadCount = feedSubscriptionService.getUnreadCount(user);

		Category root = buildCategory(null, categories, subscriptions, unreadCount);
		root.setId("all");
		root.setName("All");

		return Response.ok(root).build();
	}

	private Category buildCategory(Long id, List<FeedCategory> categories, List<FeedSubscription> subscriptions,
			Map<Long, UnreadCount> unreadCount) {
		Category category = new Category();
		category.setId(String.valueOf(id));
		category.setExpanded(true);

		for (FeedCategory c : categories) {
			if (id == null && c.getParent() == null || c.getParent() != null && Objects.equals(c.getParent().getId(), id)) {
				Category child = buildCategory(c.getId(), categories, subscriptions, unreadCount);
				child.setId(String.valueOf(c.getId()));
				child.setName(c.getName());
				child.setPosition(c.getPosition());
				if (c.getParent() != null && c.getParent().getId() != null) {
					child.setParentId(String.valueOf(c.getParent().getId()));
					child.setParentName(c.getParent().getName());
				}
				child.setExpanded(!c.isCollapsed());
				category.getChildren().add(child);
			}
		}
		category.getChildren().sort(Comparator.comparing(Category::getPosition).thenComparing(Category::getName));

		for (FeedSubscription subscription : subscriptions) {
			if (id == null && subscription.getCategory() == null
					|| subscription.getCategory() != null && Objects.equals(subscription.getCategory().getId(), id)) {
				UnreadCount uc = unreadCount.get(subscription.getId());
				Subscription sub = Subscription.build(subscription, uc);
				category.getFeeds().add(sub);
			}
		}
		category.getFeeds().sort(Comparator.comparing(Subscription::getPosition).thenComparing(Subscription::getName));

		return category;
	}

}
