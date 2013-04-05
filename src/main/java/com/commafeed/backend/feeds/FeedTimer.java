package com.commafeed.backend.feeds;

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

	@Schedule(hour = "*", minute = "*", persistent = false)
	@Lock(LockType.READ)
	private void timeout() {
		for (Feed feed : feedService.findAll()) {
			updater.update(feed);
		}
	}
}
