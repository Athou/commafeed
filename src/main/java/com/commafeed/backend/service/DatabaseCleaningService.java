package com.commafeed.backend.service;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryDAO.FeedCapacity;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.Feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contains utility methods for cleaning the database
 * 
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }) )
@Singleton
public class DatabaseCleaningService {

	private static final int BATCH_SIZE = 100;

	private final SessionFactory sessionFactory;
	private final FeedDAO feedDAO;
	private final FeedEntryDAO feedEntryDAO;
	private final FeedEntryContentDAO feedEntryContentDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;

	public long cleanFeedsWithoutSubscriptions() {
		log.info("cleaning feeds without subscriptions");
		long total = 0;
		int deleted = 0;
		long entriesTotal = 0;
		do {
			List<Feed> feeds = UnitOfWork.call(sessionFactory, () -> feedDAO.findWithoutSubscriptions(1));
			for (Feed feed : feeds) {
				int entriesDeleted = 0;
				do {
					entriesDeleted = UnitOfWork.call(sessionFactory, () -> feedEntryDAO.delete(feed.getId(), BATCH_SIZE));
					entriesTotal += entriesDeleted;
					log.info("removed {} entries for feeds without subscriptions", entriesTotal);
				} while (entriesDeleted > 0);
			}
			deleted = UnitOfWork.call(sessionFactory, () -> feedDAO.delete(feeds));
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
			deleted = UnitOfWork.call(sessionFactory, () -> feedEntryContentDAO.deleteWithoutEntries(BATCH_SIZE));
			total += deleted;
			log.info("removed {} contents without entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} contents without entries deleted", total);
		return total;
	}

	public long cleanEntriesForFeedsExceedingCapacity(final int maxFeedCapacity) {
		long total = 0;
		while (true) {
			List<FeedCapacity> feeds = UnitOfWork.call(sessionFactory,
					() -> feedEntryDAO.findFeedsExceedingCapacity(maxFeedCapacity, BATCH_SIZE));
			if (feeds.isEmpty()) {
				break;
			}

			for (final FeedCapacity feed : feeds) {
				long remaining = feed.getCapacity() - maxFeedCapacity;
				do {
					final long rem = remaining;
					int deleted = UnitOfWork.call(sessionFactory,
							() -> feedEntryDAO.deleteOldEntries(feed.getId(), Math.min(BATCH_SIZE, rem)));
					total += deleted;
					remaining -= deleted;
					log.info("removed {} entries for feeds exceeding capacity", total);
				} while (remaining > 0);
			}
		}
		log.info("cleanup done: {} entries for feeds exceeding capacity deleted", total);
		return total;
	}

	public long cleanStatusesOlderThan(final Date olderThan) {
		log.info("cleaning old read statuses");
		long total = 0;
		int deleted = 0;
		do {
			deleted = UnitOfWork.call(sessionFactory,
					() -> feedEntryStatusDAO.delete(feedEntryStatusDAO.getOldStatuses(olderThan, BATCH_SIZE)));
			total += deleted;
			log.info("removed {} old read statuses", total);
		} while (deleted != 0);
		log.info("cleanup done: {} old read statuses deleted", total);
		return total;
	}
}
