package com.commafeed.frontend.rest;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.commafeed.backend.dao.FeedCategoryService;
import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.model.FeedCategory;
import com.commafeed.model.FeedSubscription;
import com.google.common.collect.Lists;

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
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object getObject(PageParameters parameters) {
		List<FeedCategory> categories = feedCategoryService.findAll(getUser());
		Category root = new Category();
		addChildren(categories, root);
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
					int size = feedEntryService.getUnreadEntries(
							subscription.getFeed(), getUser()).size();
					sub.setUnread(size);
					child.getFeeds().add(sub);
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