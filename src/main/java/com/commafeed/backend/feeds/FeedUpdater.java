package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.model.Feed;

@Stateless
public class FeedUpdater {

	private static Logger log = LoggerFactory.getLogger(FeedTimer.class);

	@Inject
	FeedFetcher fetcher;

	@Inject
	FeedService feedService;

	@Inject
	FeedEntryService feedEntryService;

	@Asynchronous
	@Lock(LockType.READ)
	@AccessTimeout(value = 4, unit = TimeUnit.SECONDS)
	public void update(Feed feed) {
		try {
			Feed fetchedFeed = fetcher.fetch(feed.getUrl());
			if (feed.getLink() == null) {
				feed.setLink(fetchedFeed.getLink());
				feedService.update(feed);
			}
			feedEntryService.updateEntries(feed.getUrl(),
					fetchedFeed.getEntries());

			feed.setMessage(null);
			feed.setErrorCount(0);
		} catch (Exception e) {
			String message = "Unable to refresh feed " + feed.getUrl() + " : "
					+ e.getMessage();
			log.info(e.getClass() + " " + message);
			feed.setMessage(message);
			feed.setErrorCount(feed.getErrorCount() + 1);
		} finally {
			feed.setLastUpdated(Calendar.getInstance().getTime());
			feedService.update(feed);
		}
	}
}
