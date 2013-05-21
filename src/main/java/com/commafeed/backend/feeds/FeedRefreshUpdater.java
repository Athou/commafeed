package com.commafeed.backend.feeds;

import java.util.Collection;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedPushInfo;
import com.commafeed.backend.pubsubhubbub.SubscriptionHandler;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.FeedUpdateService;

@Stateless
public class FeedRefreshUpdater {

	protected static Logger log = LoggerFactory
			.getLogger(FeedRefreshUpdater.class);

	@Inject
	FeedUpdateService feedUpdateService;

	@Inject
	SubscriptionHandler handler;

	@Inject
	FeedDAO feedDAO;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Asynchronous
	public void updateEntries(Feed feed, Collection<FeedEntry> entries) {
		if (CollectionUtils.isNotEmpty(entries)) {
			feedUpdateService.updateEntries(feed, entries);
		}
		feedDAO.update(feed);
		if (applicationSettingsService.get().isPubsubhubbub()) {
			handlePubSub(feed);
		}
	}

	private void handlePubSub(Feed feed) {
		FeedPushInfo info = feed.getPushInfo();
		if (info != null && info.isActive() == false) {
			handler.subscribe(feed);
		}
	}

}
