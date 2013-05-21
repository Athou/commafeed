package com.commafeed.backend.feeds;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.LockMap;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedPushInfo;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.pubsubhubbub.SubscriptionHandler;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.FeedUpdateService;

public class FeedRefreshUpdater {

	protected static Logger log = LoggerFactory
			.getLogger(FeedRefreshUpdater.class);

	private static LockMap<String> lockMap = new LockMap<String>();

	@Inject
	FeedUpdateService feedUpdateService;

	@Inject
	SubscriptionHandler handler;

	@Inject
	FeedRefreshTaskGiver taskGiver;

	@Inject
	FeedDAO feedDAO;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	public void updateFeed(Feed feed, Collection<FeedEntry> entries) {
		taskGiver.giveBack(feed);
		if (entries != null) {
			List<FeedSubscription> subscriptions = feedSubscriptionDAO
					.findByFeed(feed);
			for (FeedEntry entry : entries) {
				updateEntry(feed, entry, subscriptions);
			}
		}

		if (applicationSettingsService.get().isPubsubhubbub()) {
			handlePubSub(feed);
		}
	}

	private void updateEntry(Feed feed, FeedEntry entry,
			List<FeedSubscription> subscriptions) {
		synchronized (lockMap.get(entry.getGuid())) {
			feedUpdateService.updateEntry(feed, entry, subscriptions);
		}
	}

	private void handlePubSub(final Feed feed) {
		FeedPushInfo info = feed.getPushInfo();
		if (info != null && info.isActive() == false) {
			new Thread() {
				@Override
				public void run() {
					handler.subscribe(feed);
				}
			}.start();
		}
	}

}
