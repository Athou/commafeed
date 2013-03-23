package com.commafeed.frontend.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.commons.lang.ObjectUtils;

import com.commafeed.frontend.rest.model.Category;
import com.commafeed.frontend.rest.model.Subscription;
import com.commafeed.model.FeedCategory;
import com.commafeed.model.FeedSubscription;

public class SubscriptionsREST extends AbstractREST {

	@Path("subscriptions")
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
