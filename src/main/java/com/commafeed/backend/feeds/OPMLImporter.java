package com.commafeed.backend.feeds;

import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.dao.FeedCategoryService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.dao.FeedSubscriptionService;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.opml.Outline;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;

public class OPMLImporter {

	@Inject
	FeedService feedService;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	FeedCategoryService feedCategoryService;

	@SuppressWarnings("unchecked")
	public void importOpml(User user, String xml) throws FeedException {

		WireFeedInput input = new WireFeedInput();
		Opml feed = (Opml) input.build(new StringReader(xml));
		List<Outline> outlines = (List<Outline>) feed.getOutlines();
		for (Outline outline : outlines) {
			handleOutline(user, outline, null);
		}
	}

	@SuppressWarnings("unchecked")
	private void handleOutline(User user, Outline outline, FeedCategory parent) {

		if (StringUtils.isEmpty(outline.getType())) {
			FeedCategory category = feedCategoryService.findByName(user,
					outline.getText(), parent);
			if (category == null) {
				category = new FeedCategory();
				category.setName(outline.getText());
				category.setParent(parent);
				category.setUser(user);
				feedCategoryService.save(category);
			}

			List<Outline> children = outline.getChildren();
			for (Outline child : children) {
				handleOutline(user, child, category);
			}
		} else {
			Feed feed = feedService.findByUrl(outline.getXmlUrl());
			if (feed == null) {
				feed = new Feed();
				feed.setUrl(outline.getXmlUrl());
				feedService.save(feed);
			}

			FeedSubscription sub = feedSubscriptionService.findByFeed(user,
					feed);
			if (sub == null) {
				sub = new FeedSubscription();
				sub.setFeed(feed);
				sub.setUser(user);
			}
			sub.setCategory(parent);
			sub.setTitle(outline.getText());
			feedSubscriptionService.saveOrUpdate(sub);
		}
	}
}
