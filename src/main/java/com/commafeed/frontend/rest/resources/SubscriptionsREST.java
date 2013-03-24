package com.commafeed.frontend.rest.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.ObjectUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.frontend.model.Category;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.SubscriptionRequest;
import com.google.common.base.Preconditions;

@Path("subscriptions")
public class SubscriptionsREST extends AbstractREST {

	@POST
	@Path("subscribe")
	public void subscribe(SubscriptionRequest req) {
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

	}

	@GET
	@Path("unsubscribe")
	public void unsubscribe(@QueryParam("id") Long subscriptionId) {
		feedSubscriptionService.deleteById(subscriptionId);
	}

	@GET
	public Category getSubscriptions() {
		Category root = new Category();

		List<FeedCategory> categories = feedCategoryService.findAll(getUser());
		addChildren(categories, root);

		root.setId("all");
		root.setName("All");

		for (FeedSubscription subscription : feedSubscriptionService
				.findWithoutCategories(getUser())) {
			Subscription sub = new Subscription();
			sub.setId(subscription.getId());
			sub.setName(subscription.getTitle());
			int size = feedEntryService.getUnreadEntries(
					subscription.getFeed(), getUser()).size();
			sub.setUnread(size);
			root.getFeeds().add(sub);
		}
		return root;
	}

	private void addChildren(List<FeedCategory> categories, Category current) {
		for (FeedCategory category : categories) {
			if ((category.getParent() == null && current.getId() == null)
					|| (category.getParent() != null && (ObjectUtils.equals(
							String.valueOf(category.getParent().getId()),
							current.getId())))) {
				Category child = new Category();
				child.setId(String.valueOf(category.getId()));
				child.setName(category.getName());
				addChildren(categories, child);
				for (FeedSubscription subscription : category
						.getSubscriptions()) {
					Subscription sub = new Subscription();
					sub.setId(subscription.getId());
					sub.setName(subscription.getTitle());
					int size = feedEntryService.getUnreadEntries(
							subscription.getFeed(), getUser()).size();
					sub.setUnread(size);
					child.getFeeds().add(sub);
				}
				current.getChildren().add(child);
			}
		}
	}

}
