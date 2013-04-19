package com.commafeed.frontend.rest.resources;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.model.Category;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.request.AddCategoryRequest;
import com.commafeed.frontend.model.request.CollapseRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.RenameRequest;
import com.commafeed.frontend.rest.Enums.ReadType;
import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/category")
@Api(value = "/category", description = "Operations about user categories")
public class CategoryREST extends AbstractResourceREST {

	public static final String ALL = "all";

	@Path("/entries")
	@GET
	@ApiOperation(value = "Get category entries", notes = "Get a list of category entries", responseClass = "com.commafeed.frontend.model.Entries")
	public Entries getCategoryEntries(
			@ApiParam(value = "id of the category, or 'all'", required = true) @QueryParam("id") String id,
			@ApiParam(value = "all entries or only unread ones", allowableValues = "all,unread", required = true) @QueryParam("readType") ReadType readType,
			@ApiParam(value = "offset for paging") @DefaultValue("0") @QueryParam("offset") int offset,
			@ApiParam(value = "limit for paging") @DefaultValue("-1") @QueryParam("limit") int limit,
			@ApiParam(value = "date ordering", allowableValues = "asc,desc") @QueryParam("order") @DefaultValue("desc") ReadingOrder order) {

		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(readType);

		Entries entries = new Entries();
		boolean unreadOnly = readType == ReadType.unread;

		if (ALL.equals(id)) {
			entries.setName("All");
			List<FeedEntryStatus> unreadEntries = feedEntryStatusDAO.findAll(
					getUser(), unreadOnly, offset, limit, order, true);
			for (FeedEntryStatus status : unreadEntries) {
				entries.getEntries().add(Entry.build(status));
			}

		} else {
			FeedCategory feedCategory = feedCategoryDAO.findById(getUser(),
					Long.valueOf(id));
			if (feedCategory != null) {
				List<FeedCategory> childrenCategories = feedCategoryDAO
						.findAllChildrenCategories(getUser(), feedCategory);
				List<FeedEntryStatus> unreadEntries = feedEntryStatusDAO
						.findByCategories(childrenCategories, getUser(),
								unreadOnly, offset, limit, order, true);
				for (FeedEntryStatus status : unreadEntries) {
					entries.getEntries().add(Entry.build(status));
				}
				entries.setName(feedCategory.getName());
			}

		}
		entries.setTimestamp(Calendar.getInstance().getTimeInMillis());
		return entries;
	}

	@Path("/mark")
	@POST
	@ApiOperation(value = "Mark category entries", notes = "Mark feed entries of this category as read")
	public Response markCategoryEntries(
			@ApiParam(value = "category id, or 'all'", required = true) MarkRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		Date olderThan = req.getOlderThan() == null ? null : new Date(
				req.getOlderThan());

		if (ALL.equals(req.getId())) {
			feedEntryStatusDAO.markAllEntries(getUser(), olderThan);
		} else {
			List<FeedCategory> categories = feedCategoryDAO
					.findAllChildrenCategories(
							getUser(),
							feedCategoryDAO.findById(getUser(),
									Long.valueOf(req.getId())));
			feedEntryStatusDAO.markCategoryEntries(getUser(), categories,
					olderThan);
		}

		return Response.ok(Status.OK).build();
	}

	@Path("/add")
	@POST
	@ApiOperation(value = "Add a category", notes = "Add a new feed category")
	public Response addCategory(
			@ApiParam(required = true) AddCategoryRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getName());

		FeedCategory cat = new FeedCategory();
		cat.setName(req.getName());
		cat.setUser(getUser());
		String parentId = req.getParentId();
		if (parentId != null && !ALL.equals(parentId)) {
			FeedCategory parent = new FeedCategory();
			parent.setId(Long.valueOf(parentId));
			cat.setParent(parent);
		}
		feedCategoryDAO.save(cat);
		return Response.ok().build();
	}

	@POST
	@Path("/delete")
	@ApiOperation(value = "Delete a category", notes = "Delete an existing feed category")
	public Response deleteCategory(@ApiParam(required = true) IDRequest req) {

		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		FeedCategory cat = feedCategoryDAO.findById(getUser(), req.getId());
		if (cat != null) {
			List<FeedSubscription> subs = feedSubscriptionDAO.findByCategory(
					getUser(), cat);
			for (FeedSubscription sub : subs) {
				sub.setCategory(null);
			}
			feedSubscriptionDAO.update(subs);
			feedCategoryDAO.delete(cat);
			return Response.ok().build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@POST
	@Path("/rename")
	@ApiOperation(value = "Rename a category", notes = "Rename an existing feed category")
	public Response renameCategory(@ApiParam(required = true) RenameRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());
		Preconditions.checkArgument(StringUtils.isNotBlank(req.getName()));

		FeedCategory category = feedCategoryDAO
				.findById(getUser(), req.getId());
		category.setName(req.getName());
		feedCategoryDAO.update(category);

		return Response.ok(Status.OK).build();
	}

	@POST
	@Path("/collapse")
	@ApiOperation(value = "Collapse a category", notes = "Save collapsed or expanded status for a category")
	public Response collapse(@ApiParam(required = true) CollapseRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getId());

		FeedCategory category = feedCategoryDAO.findById(getUser(),
				Long.valueOf(req.getId()));
		category.setCollapsed(req.isCollapse());
		feedCategoryDAO.update(category);

		return Response.ok(Status.OK).build();
	}

	@GET
	@Path("/get")
	@ApiOperation(value = "Get feed categories", notes = "Get all categories and subscriptions of the user", responseClass = "com.commafeed.frontend.model.Category")
	public Category getSubscriptions() {

		List<FeedCategory> categories = feedCategoryDAO.findAll(getUser());
		List<FeedSubscription> subscriptions = feedSubscriptionDAO
				.findAll(getUser());
		Map<Long, Long> unreadCount = feedEntryStatusDAO
				.getUnreadCount(getUser());

		Category root = buildCategory(null, categories, subscriptions,
				unreadCount);
		root.setId("all");
		root.setName("All");

		return root;
	}

	private Category buildCategory(Long id, List<FeedCategory> categories,
			List<FeedSubscription> subscriptions, Map<Long, Long> unreadCount) {
		Category category = new Category();
		category.setId(String.valueOf(id));
		category.setExpanded(true);

		for (FeedCategory c : categories) {
			if ((id == null && c.getParent() == null)
					|| (c.getParent() != null && ObjectUtils.equals(c
							.getParent().getId(), id))) {
				Category child = buildCategory(c.getId(), categories,
						subscriptions, unreadCount);
				child.setId(String.valueOf(c.getId()));
				child.setName(c.getName());
				child.setExpanded(!c.isCollapsed());
				category.getChildren().add(child);
			}
		}
		Collections.sort(category.getChildren(), new Comparator<Category>() {
			@Override
			public int compare(Category o1, Category o2) {
				return ObjectUtils.compare(o1.getName(), o2.getName());
			}
		});

		for (FeedSubscription subscription : subscriptions) {
			if ((id == null && subscription.getCategory() == null)
					|| (subscription.getCategory() != null && ObjectUtils
							.equals(subscription.getCategory().getId(), id))) {
				Subscription sub = new Subscription();
				sub.setId(subscription.getId());
				sub.setName(subscription.getTitle());
				sub.setMessage(subscription.getFeed().getMessage());
				sub.setErrorCount(subscription.getFeed().getErrorCount());
				sub.setFeedUrl(subscription.getFeed().getLink());
				Long size = unreadCount.get(subscription.getId());
				sub.setUnread(size == null ? 0 : size);
				category.getFeeds().add(sub);
			}
		}
		Collections.sort(category.getFeeds(), new Comparator<Subscription>() {
			@Override
			public int compare(Subscription o1, Subscription o2) {
				return ObjectUtils.compare(o1.getName(), o2.getName());
			}
		});
		return category;
	}

}
