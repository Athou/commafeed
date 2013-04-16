package com.commafeed.frontend.rest.resources;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
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
import org.apache.commons.lang.ObjectUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.frontend.model.Category;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.SubscriptionRequest;
import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/subscriptions")
@Api(value = "/subscriptions", description = "Operations about user feed subscriptions")
public class SubscriptionsREST extends AbstractREST {

	@GET
	@Path("/feed/fetch")
	@ApiOperation(value = "Fetch a feed", notes = "Fetch a feed by its url", responseClass = "com.commafeed.backend.model.Feed")
	public Feed fetchFeed(@QueryParam("url") String url) {
		Preconditions.checkNotNull(url);

		url = prependHttp(url);
		Feed feed = null;
		try {
			feed = feedFetcher.fetch(url, true);
		} catch (Exception e) {
			throw new WebApplicationException(e, Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build());
		}
		return feed;
	}

	@POST
	@Path("/feed/subscribe")
	@ApiOperation(value = "Subscribe to a feed", notes = "Subscribe to a feed")
	public Response subscribe(
			@ApiParam(value = "subscription request", required = true) SubscriptionRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getTitle());
		Preconditions.checkNotNull(req.getUrl());

		String url = prependHttp(req.getUrl());
		url = fetchFeed(url).getUrl();

		FeedCategory category = EntriesREST.ALL.equals(req.getCategoryId()) ? null
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

	@GET
	@Path("/feed/unsubscribe")
	@ApiOperation(value = "Unsubscribe to a feed", notes = "Unsubscribe to a feed")
	public Response unsubscribe(
			@ApiParam(value = "subscription id", required = true) @QueryParam("id") Long subscriptionId) {
		FeedSubscription sub = feedSubscriptionDAO.findById(getUser(),
				subscriptionId);
		if (sub != null) {
			feedSubscriptionDAO.delete(sub);
			return Response.ok(Status.OK).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@GET
	@Path("/feed/rename")
	@ApiOperation(value = "Rename a subscription", notes = "Rename a feed subscription")
	public Response rename(
			@ApiParam(value = "subscription id", required = true) @QueryParam("id") Long id,
			@ApiParam(value = "new name", required = true) @QueryParam("name") String name) {
		FeedSubscription subscription = feedSubscriptionDAO.findById(getUser(),
				id);
		subscription.setTitle(name);
		feedSubscriptionDAO.update(subscription);

		return Response.ok(Status.OK).build();
	}

	@Path("/category/add")
	@GET
	@ApiOperation(value = "Add a category", notes = "Add a new feed category")
	public Response addCategory(
			@ApiParam(value = "new name", required = true) @QueryParam("name") String name,
			@ApiParam(value = "parent category id, if any") @QueryParam("parentId") String parentId) {
		Preconditions.checkNotNull(name);

		FeedCategory cat = new FeedCategory();
		cat.setName(name);
		cat.setUser(getUser());
		if (parentId != null && !EntriesREST.ALL.equals(parentId)) {
			FeedCategory parent = new FeedCategory();
			parent.setId(Long.valueOf(parentId));
			cat.setParent(parent);
		}
		feedCategoryDAO.save(cat);
		return Response.ok().build();
	}

	@GET
	@Path("/category/delete")
	@ApiOperation(value = "Delete a category", notes = "Delete an existing feed category")
	public Response deleteCategory(
			@ApiParam(value = "category id", required = true) @QueryParam("id") Long id) {
		FeedCategory cat = feedCategoryDAO.findById(getUser(), id);
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

	@GET
	@Path("/category/rename")
	@ApiOperation(value = "Rename a category", notes = "Rename an existing feed category")
	public Response renameCategory(
			@ApiParam(value = "category id", required = true) @QueryParam("id") Long id,
			@ApiParam(value = "new name", required = true) @QueryParam("name") String name) {

		FeedCategory category = feedCategoryDAO.findById(getUser(), id);
		category.setName(name);
		feedCategoryDAO.update(category);

		return Response.ok(Status.OK).build();
	}

	@GET
	@Path("/category/collapse")
	@ApiOperation(value = "Collapse a category", notes = "Save collapsed or expanded status for a category")
	public Response collapse(
			@ApiParam(value = "category id", required = true) @QueryParam("id") String id,
			@ApiParam(value = "true if collapsed", required = true) @QueryParam("collapse") boolean collapse) {
		Preconditions.checkNotNull(id);
		if (!EntriesREST.ALL.equals(id)) {
			FeedCategory category = feedCategoryDAO.findById(getUser(),
					Long.valueOf(id));
			category.setCollapsed(collapse);
			feedCategoryDAO.update(category);
		}
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

	@GET
	@Path("/get")
	@ApiOperation(value = "Get feed subscriptions", notes = "Get all subscriptions of the user", responseClass = "com.commafeed.frontend.model.Category")
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
