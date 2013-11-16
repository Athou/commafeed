package com.commafeed.backend.services;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;

/**
 * Contains utility methods for cleaning the database
 * 
 */
@Slf4j
public class DatabaseCleaningService {

	@Inject
	FeedDAO feedDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	FeedEntryContentDAO feedEntryContentDAO;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public long cleanEntriesForFeedsWithoutSubscriptions() {
		log.info("cleaning entries for feeds without subscriptions");
		long total = 0;
		int deleted = 0;
		do {
			deleted = 0;
			List<Feed> feeds = feedDAO.findWithoutSubscriptions(1);
			for (Feed feed : feeds) {
				deleted += feedEntryDAO.delete(feed, 10);
				total += deleted;
				log.info("removed {} entries for feeds without subscriptions", total);
			}
		} while (deleted != 0);
		log.info("cleanup done: {} entries for feeds without subscriptions deleted", total);
		return total;
	}

	public long cleanFeedsWithoutSubscriptions() {
		log.info("cleaning feeds without subscriptions");
		long total = 0;
		int deleted = 0;
		do {
			deleted = feedDAO.delete(feedDAO.findWithoutSubscriptions(10));
			total += deleted;
			log.info("removed {} feeds without subscriptions", total);
		} while (deleted != 0);
		log.info("cleanup done: {} feeds without subscriptions deleted", total);
		return total;
	}

	public long cleanContentsWithoutEntries() {
		log.info("cleaning contents without entries");
		long total = 0;
		int deleted = 0;
		do {
			deleted = feedEntryContentDAO.deleteWithoutEntries(10);
			total += deleted;
			log.info("removed {} contents without entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} contents without entries deleted", total);
		return total;
	}

	public long cleanEntriesOlderThan(long value, TimeUnit unit) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1 * (int) unit.toMinutes(value));

		long total = 0;
		int deleted = 0;
		do {
			deleted = feedEntryDAO.delete(cal.getTime(), 100);
			total += deleted;
			log.info("removed {} entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} entries deleted", total);
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
			feedDAO.delete(feed);
		}
		feedDAO.saveOrUpdate(into);
	}

	public long cleanStatusesOlderThan(Date olderThan) {
		log.info("cleaning old read statuses");
		long total = 0;
		List<FeedEntryStatus> list = Collections.emptyList();
		do {
			list = feedEntryStatusDAO.getOldStatuses(olderThan, 100);
			if (!list.isEmpty()) {
				feedEntryStatusDAO.delete(list);
				total += list.size();
				log.info("cleaned {} old read statuses", total);
			}
		} while (!list.isEmpty());
		log.info("cleanup done: {} old read statuses deleted", total);
		return total;
	}
}
