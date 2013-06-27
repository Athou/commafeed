package com.commafeed.backend.services;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryDAO.EntryWithFeed;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.google.common.collect.Lists;

@Stateless
public class FeedUpdateService {

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	MetricsBean metricsBean;

	public void updateEntry(Feed feed, FeedEntry entry,
			List<FeedSubscription> subscriptions) {

		EntryWithFeed existing = feedEntryDAO.findExisting(entry.getGuid(),
				entry.getUrl(), feed.getId());

		FeedEntry update = null;
		if (existing == null) {
			entry.setAuthor(FeedUtils.truncate(FeedUtils.handleContent(
					entry.getAuthor(), feed.getLink(), true), 128));
			FeedEntryContent content = entry.getContent();
			content.setTitle(FeedUtils.truncate(FeedUtils.handleContent(
					content.getTitle(), feed.getLink(), true), 2048));
			content.setContent(FeedUtils.handleContent(content.getContent(),
					feed.getLink(), false));

			entry.setInserted(new Date());
			entry.getFeeds().add(feed);

			update = entry;
		} else if (existing.feed == null) {
			existing.entry.getFeeds().add(feed);
			update = existing.entry;
		}

		if (update != null) {
			List<FeedEntryStatus> statusUpdateList = Lists.newArrayList();
			for (FeedSubscription sub : subscriptions) {
				FeedEntryStatus status = new FeedEntryStatus();
				status.setEntry(update);
				status.setSubscription(sub);
				statusUpdateList.add(status);
			}
			feedEntryDAO.saveOrUpdate(update);
			feedEntryStatusDAO.saveOrUpdate(statusUpdateList);
			metricsBean.entryUpdated(statusUpdateList.size());
		}
	}
}
