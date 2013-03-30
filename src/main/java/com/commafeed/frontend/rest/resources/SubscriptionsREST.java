package com.commafeed.frontend.rest.resources;

import java.util.List;

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
import com.sun.syndication.io.FeedException;

@Path("subscriptions")
public class SubscriptionsREST extends AbstractREST {

	@GET
	@Path("fetch")
	public Feed fetchFeed(@QueryParam("url") String url) {
		Preconditions.checkNotNull(url);
		Feed feed = null;
		try {
			feed = feedFetcher.fetch(url);
		} catch (FeedException e) {
			throw new WebApplicationException(Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage()).build());
		}
		return feed;
	}

	@POST
	@Path("subscribe")
	public Response subscribe(SubscriptionRequest req) {
		Preconditions.checkNotNull(req);
		Preconditions.checkNotNull(req.getTitle());
		Preconditions.checkNotNull(req.getUrl());

		Feed feed = feedService.findByUrl(req.getUrl());
		if (feed == null) {
			feed = new Feed();
			feed.setUrl(req.getUrl());
			feedService.save(feed);
		}

		FeedSubscription sub = new FeedSubscription();
		sub.setCategory("all".equals(req.getCategoryId()) ? null
				: feedCategoryService.findById(Long.valueOf(req.getCategoryId())));
		sub.setFeed(feed);
		sub.setTitle(req.getTitle());
		sub.setUser(getUser());
		feedSubscriptionService.save(sub);
		return Response.ok(Status.OK).build();
	}

	@GET
	@Path("unsubscribe")
	public Response unsubscribe(@QueryParam("id") Long subscriptionId) {
		feedSubscriptionService.deleteById(subscriptionId);
		return Response.ok(Status.OK).build();
	}

	@GET
	@Path("collapse")
	public Response collapse(@QueryParam("id") String id,
			@QueryParam("collapse") boolean collapse) {
		Preconditions.checkNotNull(id);
		if (!"all".equals(id)) {
			FeedCategory category = feedCategoryService.findById(getUser(),
					Long.valueOf(id));
			category.setCollapsed(collapse);
			feedCategoryService.update(category);
		}
		return Response.ok(Status.OK).build();
	}

	@POST
	@Path("import")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@SuppressWarnings("unchecked")
	public Response importOpml() {
		try {
			FileItemFactory factory = new DiskFileItemFactory(1000000, null);
			ServletFileUpload upload = new ServletFileUpload(factory);
			List<FileItem> items = upload.parseRequest(request);
			for (FileItem item : items) {
				if ("file".equals(item.getFieldName())) {
					opmlImporter.importOpml(getUser(),
							IOUtils.toString(item.getInputStream(), "UTF-8"));
					break;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return Response.ok(Status.OK).build();
	}

	@GET
	public Category getSubscriptions() {

		List<FeedCategory> categories = feedCategoryService.findAll(getUser());
		List<FeedSubscription> subscriptions = feedSubscriptionService
				.findAll(getUser());

		Category root = buildCategory(null, categories, subscriptions);
		root.setId("all");
		root.setName("All");

		return root;
	}

	Category buildCategory(Long id, List<FeedCategory> categories,
			List<FeedSubscription> subscriptions) {
		Category category = new Category();
		category.setId(String.valueOf(id));
		category.setExpanded(true);

		for (FeedCategory c : categories) {
			if ((id == null && c.getParent() == null)
					|| (c.getParent() != null && ObjectUtils.equals(c
							.getParent().getId(), id))) {
				Category child = buildCategory(c.getId(), categories,
						subscriptions);
				child.setId(String.valueOf(c.getId()));
				child.setName(c.getName());
				child.setExpanded(!c.isCollapsed());
				category.getChildren().add(child);
			}
		}

		for (FeedSubscription subscription : subscriptions) {
			if ((id == null && subscription.getCategory() == null)
					|| (subscription.getCategory() != null && ObjectUtils
							.equals(subscription.getCategory().getId(), id))) {
				Subscription sub = new Subscription();
				sub.setId(subscription.getId());
				sub.setName(subscription.getTitle());
				sub.setMessage(subscription.getFeed().getMessage());
				int size = feedEntryService.getEntries(subscription.getFeed(),
						getUser(), true).size();
				sub.setUnread(size);
				category.getFeeds().add(sub);
			}
		}
		return category;
	}

}
