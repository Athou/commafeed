package com.commafeed.backend.feeds;

import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.model.Feed;

@Singleton
public class FeedTimer {

	@Inject
	FeedService feedService;

	@Inject
	FeedUpdater updater;

	// every five seconds
	@Schedule(hour = "*", minute = "*", second = "*/5", persistent = false)
	@Lock(LockType.READ)
	private void timeout() {
		double count = feedService.getCount() * 5d / 60d;
		List<Feed> feeds = feedService.findNextUpdatable((int) count);
		for (Feed feed : feeds) {
			updater.update(feed);
		}
	}
}
