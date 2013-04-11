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

	private long count = -1;

	// every seconds
	@Schedule(hour = "*", minute = "*", second = "*/5", persistent = false)
	@Lock(LockType.READ)
	private void timeout() {
		if (count == -1) {
			refreshCount();
		}
		if (count > 0) {
			int updateCount = (int) Math.ceil(count * 5d / 60d);
			List<Feed> feeds = feedService.findNextUpdatable(updateCount);
			for (Feed feed : feeds) {
				updater.update(feed);
			}
		}
	}

	@Schedule(hour = "*", minute = "0", persistent = false)
	private void refreshCount() {
		count = feedService.getCount();
	}
}
