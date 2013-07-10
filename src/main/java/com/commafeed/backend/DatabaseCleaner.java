package com.commafeed.backend;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedDAO.FeedCount;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedSubscription;
import com.google.common.collect.Lists;

public class DatabaseCleaner {

	private static Logger log = LoggerFactory.getLogger(DatabaseCleaner.class);

	@Inject
	FeedDAO feedDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	public long cleanFeedsWithoutSubscriptions() {

		long total = 0;
		int deleted = -1;
		do {
			deleted = feedDAO.deleteWithoutSubscriptions(10);
			total += deleted;
			log.info("removed {} feeds without subscriptions", total);
		} while (deleted != 0);
		log.info("cleanup done: {} feeds without subscriptions deleted", total);
		return total;
	}

	public long cleanEntriesWithoutFeeds() {

		long total = 0;
		int deleted = -1;
		do {
			deleted = feedEntryDAO.deleteWithoutFeeds(100);
			total += deleted;
			log.info("removed {} entries without feeds", total);
		} while (deleted != 0);
		log.info("cleanup done: {} entries without feeds deleted", total);
		return total;
	}

	public long cleanEntriesOlderThan(long value, TimeUnit unit) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1 * (int) unit.toMinutes(value));

		long total = 0;
		int deleted = -1;
		do {
			deleted = feedEntryDAO.delete(cal.getTime(), 100);
			total += deleted;
			log.info("removed {} entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} entries deleted", total);
		return total;
	}

	public long cleanDuplicateFeeds() {
		long total = 0;
		int deleted = -1;
		do {
			List<FeedCount> fcs = feedDAO.findDuplicates(0, 10, 1);
			deleted = fcs.size();
			for (final FeedCount fc : fcs) {
				new Thread() {
					public void run() {
						Feed into = feedDAO.findById(fc.feeds.get(0).getId());
						List<Feed> feeds = Lists.newArrayList();
						for (Feed feed : fc.feeds) {
							feeds.add(feedDAO.findById(feed.getId()));
						}
						mergeFeeds(into, feeds);
					};
				}.start();
			}
			total += deleted;
			log.info("merged {} feeds", total);
		} while (deleted != 0);
		log.info("cleanup done: {} feeds merged", total);
		return total;
	}

	public void mergeFeeds(Feed into, List<Feed> feeds) {
		for (Feed feed : feeds) {
			if (into.getId().equals(feed.getId())) {
				continue;
			}
			List<FeedSubscription> subs = feedSubscriptionDAO.findByFeed(feed);
			for (FeedSubscription sub : subs) {
				sub.setFeed(into);
			}
			feedSubscriptionDAO.saveOrUpdate(subs);
			feedDAO.deleteRelationships(feed);
			feedDAO.delete(feed);
		}
		feedDAO.saveOrUpdate(into);
	}
}
