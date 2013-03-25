package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Future;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.model.Feed;
import com.google.common.collect.Maps;

@Singleton
public class FeedTimer {

	private static Logger log = LoggerFactory.getLogger(FeedTimer.class);

	@Inject
	FeedService feedService;

	@Inject
	FeedEntryService feedEntryService;

	@Inject
	FeedFetcher fetcher;

	@Schedule(hour = "*", minute = "*", persistent = false)
	private void timeout() {
		Map<String, Feed> feeds = Maps.newHashMap();
		for (Feed feed : feedService.findAll()) {
			feeds.put(feed.getUrl(), feed);
		}

		Map<String, Future<Feed>> futures = Maps.newHashMap();
		for (Feed feed : feeds.values()) {
			Future<Feed> future = fetcher.fetch(feed.getUrl());
			futures.put(feed.getUrl(), future);
		}

		for (String key : futures.keySet()) {
			Future<Feed> future = futures.get(key);
			try {
				Feed feed = future.get();
				feedEntryService
						.updateEntries(feed.getUrl(), feed.getEntries());
			} catch (Exception e) {
				log.info("Unable to refresh feed " + key + " : "
						+ e.getMessage());

				Feed feed = feeds.get(key);
				feed.setLastUpdated(Calendar.getInstance().getTime());
				feed.setMessage("Unable to refresh feed: " + e.getMessage());
				feedService.update(feed);
			}
		}
	}

}
