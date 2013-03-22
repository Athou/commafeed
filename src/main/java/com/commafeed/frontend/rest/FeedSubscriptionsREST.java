package com.commafeed.frontend.rest;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.backend.dao.FeedCategoryService;
import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.frontend.rest.model.Category;
import com.commafeed.frontend.rest.model.Subscription;
import com.commafeed.model.FeedCategory;
import com.commafeed.model.FeedSubscription;

@SuppressWarnings("serial")
public class FeedSubscriptionsREST extends JSONPage {

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	FeedCategoryService feedCategoryService;

	@Inject
	FeedEntryService feedEntryService;

	public FeedSubscriptionsREST(PageParameters pageParameters) {
		super(pageParameters);
	}

	@Override
	protected Object getObject(PageParameters parameters) {
		List<FeedCategory> categories = feedCategoryService.findAll(getUser());
		Category root = new Category();
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