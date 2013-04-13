package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.List;
import java.util.Queue;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.google.common.collect.Lists;

@Singleton
public class FeedRefreshTaskGiver {

	@Inject
	FeedDAO feedDAO;

	private Queue<Feed> queue = Lists.newLinkedList();

	@Lock(LockType.WRITE)
	public Feed take() {
		if (queue.peek() == null) {
			List<Feed> feeds = feedDAO.findNextUpdatable(30);
			for (Feed feed : feeds) {
				queue.add(feed);
				feed.setLastUpdated(Calendar.getInstance().getTime());
			}
			feedDAO.update(feeds);
		}
		return queue.poll();
	}
}
