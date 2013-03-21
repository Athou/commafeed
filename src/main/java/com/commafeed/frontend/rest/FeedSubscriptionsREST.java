package com.commafeed.frontend.rest;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;

import com.commafeed.backend.dao.FeedCategoryService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.pages.JSONPage;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.commafeed.model.FeedCategory;
import com.commafeed.model.FeedSubscription;
import com.commafeed.model.User;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class FeedSubscriptionsREST extends JSONPage {

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	FeedCategoryService FeedCategoryService;

	@Override
	protected Object getObject() {

		User user = CommaFeedSession.get().getUser();
		List<FeedCategory> categories = FeedCategoryService.findAll(user);

		Category root = new Category();
		addChildren(categories, root);
		for (FeedSubscription subscription : feedSubscriptionService
				.findByField(MF.i(MF.p(FeedSubscription.class).getCategory()),
						null)) {
			Subscription sub = new Subscription();
			sub.setId(subscription.getId());
			sub.setName(subscription.getTitle());
			sub.setUnread(77);
		}
		return root;
	}

	private void addChildren(List<FeedCategory> categories, Category current) {
		for (FeedCategory category : categories) {
			if ((category.getParent() == null && current.getId() == null)
					|| (category.getParent() != null && (ObjectUtils.equals(
							category.getParent().getId(), current.getId())))) {
				Category child = new Category();
				child.setId(category.getId());
				child.setName(category.getName());
				addChildren(categories, child);
				for (FeedSubscription subscription : category
						.getSubscriptions()) {
					Subscription sub = new Subscription();
					sub.setId(subscription.getId());
					sub.setName(subscription.getTitle());
					sub.setUnread(77);
				}
				current.getChildren().add(child);
			}
		}
	}

	public static class Category {
		private Long id;
		private String name;
		private List<Category> children = Lists.newArrayList();
		private List<Subscription> feeds = Lists.newArrayList();

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<Category> getChildren() {
			return children;
		}

		public void setChildren(List<Category> children) {
			this.children = children;
		}

		public List<Subscription> getFeeds() {
			return feeds;
		}

		public void setFeeds(List<Subscription> feeds) {
			this.feeds = feeds;
		}

	}

	public static class Subscription {
		private Long id;
		private String name;
		private int unread;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getUnread() {
			return unread;
		}

		public void setUnread(int unread) {
			this.unread = unread;
		}

	}

}