package com.commafeed.backend.opml;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.google.common.base.MoreObjects;
import com.rometools.opml.feed.opml.Attribute;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class OPMLExporter {

	private final FeedCategoryDAO feedCategoryDAO;
	private final FeedSubscriptionDAO feedSubscriptionDAO;

	public Opml export(User user) {
		Opml opml = new Opml();
		opml.setFeedType("opml_1.1");
		opml.setTitle(String.format("%s subscriptions in CommaFeed", user.getName()));
		opml.setCreated(new Date());

		List<FeedCategory> categories = feedCategoryDAO.findAll(user);
		Collections.sort(categories,
				(e1, e2) -> MoreObjects.firstNonNull(e1.getPosition(), 0) - MoreObjects.firstNonNull(e2.getPosition(), 0));

		List<FeedSubscription> subscriptions = feedSubscriptionDAO.findAll(user);
		Collections.sort(subscriptions,
				(e1, e2) -> MoreObjects.firstNonNull(e1.getPosition(), 0) - MoreObjects.firstNonNull(e2.getPosition(), 0));

		// export root categories
		for (FeedCategory cat : categories.stream().filter(c -> c.getParent() == null).collect(Collectors.toList())) {
			opml.getOutlines().add(buildCategoryOutline(cat, categories, subscriptions));
		}

		// export root subscriptions
		for (FeedSubscription sub : subscriptions.stream().filter(s -> s.getCategory() == null).collect(Collectors.toList())) {
			opml.getOutlines().add(buildSubscriptionOutline(sub));
		}

		return opml;

	}

	private Outline buildCategoryOutline(FeedCategory cat, List<FeedCategory> categories, List<FeedSubscription> subscriptions) {
		Outline outline = new Outline();
		outline.setText(cat.getName());
		outline.setTitle(cat.getName());

		for (FeedCategory child : categories.stream().filter(c -> c.getParent() != null && c.getParent().getId().equals(cat.getId()))
				.collect(Collectors.toList())) {
			outline.getChildren().add(buildCategoryOutline(child, categories, subscriptions));
		}

		for (FeedSubscription sub : subscriptions.stream()
				.filter(s -> s.getCategory() != null && s.getCategory().getId().equals(cat.getId())).collect(Collectors.toList())) {
			outline.getChildren().add(buildSubscriptionOutline(sub));
		}
		return outline;
	}

	private Outline buildSubscriptionOutline(FeedSubscription sub) {
		Outline outline = new Outline();
		outline.setText(sub.getTitle());
		outline.setTitle(sub.getTitle());
		outline.setType("rss");
		outline.getAttributes().add(new Attribute("xmlUrl", sub.getFeed().getUrl()));
		if (sub.getFeed().getLink() != null) {
			outline.getAttributes().add(new Attribute("htmlUrl", sub.getFeed().getLink()));
		}
		return outline;
	}
}
