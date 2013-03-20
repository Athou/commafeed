package com.commafeed.backend.feeds;

import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.model.Feed;
import com.google.common.collect.Lists;

@Singleton
public class FeedTimer {

	@Inject
	FeedService feedService;

	@Inject
	FeedEntryService feedEntryService;

	@Inject
	FeedFetcher fetcher;

	@Schedule(hour = "*", minute = "*", persistent = false)
	private void timeout() {
		List<Feed> feeds = feedService.findAll();

		List<Future<Feed>> futures = Lists.newArrayList();
		for (Feed feed : feeds) {
			Future<Feed> future = fetcher.fetch(feed.getUrl());
			futures.add(future);
		}

		for (Future<Feed> future : futures) {
			try {
				Feed feed = future.get();
				feedEntryService
						.updateEntries(feed.getUrl(), feed.getEntries());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
