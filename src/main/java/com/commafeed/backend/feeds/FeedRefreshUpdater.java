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
import com.commafeed.backend.model.FeedEntryContent;
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
			for (FeedEntry entry : entries) {
				handleEntry(feed, entry);
			}
			feedUpdateService.updateEntries(feed, entries);
		}
		feedDAO.update(feed);
		if (applicationSettingsService.get().isPubsubhubbub()) {
			handlePubSub(feed);
		}
	}

	private void handleEntry(Feed feed, FeedEntry entry) {
		String baseUri = feed.getLink();
		FeedEntryContent content = entry.getContent();

		content.setContent(FeedUtils.handleContent(content.getContent(),
				baseUri));
		String title = FeedUtils.handleContent(content.getTitle(), baseUri);
		if (title != null) {
			content.setTitle(title.substring(0, Math.min(2048, title.length())));
		}
		String author = entry.getAuthor();
		if (author != null) {
			entry.setAuthor(author.substring(0, Math.min(128, author.length())));
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
