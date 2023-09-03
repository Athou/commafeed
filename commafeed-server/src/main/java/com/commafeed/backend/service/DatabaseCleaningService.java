package com.commafeed.backend.service;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryDAO.FeedCapacity;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.Feed;

import lombok.extern.slf4j.Slf4j;

/**
 * Contains utility methods for cleaning the database
 * 
 */
@Slf4j
@Singleton
public class DatabaseCleaningService {

	private final int batchSize;

	private final UnitOfWork unitOfWork;
	private final FeedDAO feedDAO;
	private final FeedEntryDAO feedEntryDAO;
	private final FeedEntryContentDAO feedEntryContentDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final Meter entriesDeletedMeter;

	@Inject
	public DatabaseCleaningService(CommaFeedConfiguration config, UnitOfWork unitOfWork, FeedDAO feedDAO, FeedEntryDAO feedEntryDAO,
			FeedEntryContentDAO feedEntryContentDAO, FeedEntryStatusDAO feedEntryStatusDAO, MetricRegistry metrics) {
		this.unitOfWork = unitOfWork;
		this.feedDAO = feedDAO;
		this.feedEntryDAO = feedEntryDAO;
		this.feedEntryContentDAO = feedEntryContentDAO;
		this.feedEntryStatusDAO = feedEntryStatusDAO;
		this.batchSize = config.getApplicationSettings().getDatabaseCleanupBatchSize();
		this.entriesDeletedMeter = metrics.meter(MetricRegistry.name(getClass(), "entriesDeleted"));
	}

	public void cleanFeedsWithoutSubscriptions() {
		log.info("cleaning feeds without subscriptions");
		long total = 0;
		int deleted;
		long entriesTotal = 0;
		do {
			List<Feed> feeds = unitOfWork.call(() -> feedDAO.findWithoutSubscriptions(1));
			for (Feed feed : feeds) {
				long entriesDeleted;
				do {
					entriesDeleted = unitOfWork.call(() -> feedEntryDAO.delete(feed.getId(), batchSize));
					entriesDeletedMeter.mark(entriesDeleted);
					entriesTotal += entriesDeleted;
					log.info("removed {} entries for feeds without subscriptions", entriesTotal);
				} while (entriesDeleted > 0);
			}
			deleted = unitOfWork.call(() -> feedDAO.delete(feeds));
			total += deleted;
			log.info("removed {} feeds without subscriptions", total);
		} while (deleted != 0);
		log.info("cleanup done: {} feeds without subscriptions deleted", total);
	}

	public void cleanContentsWithoutEntries() {
		log.info("cleaning contents without entries");
		long total = 0;
		long deleted;
		do {
			deleted = unitOfWork.call(() -> feedEntryContentDAO.deleteWithoutEntries(batchSize));
			total += deleted;
			log.info("removed {} contents without entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} contents without entries deleted", total);
	}

	public void cleanEntriesForFeedsExceedingCapacity(final int maxFeedCapacity) {
		long total = 0;
		while (true) {
			List<FeedCapacity> feeds = unitOfWork.call(() -> feedEntryDAO.findFeedsExceedingCapacity(maxFeedCapacity, batchSize));
			if (feeds.isEmpty()) {
				break;
			}

			for (final FeedCapacity feed : feeds) {
				long remaining = feed.getCapacity() - maxFeedCapacity;
				do {
					final long rem = remaining;
					int deleted = unitOfWork.call(() -> feedEntryDAO.deleteOldEntries(feed.getId(), Math.min(batchSize, rem)));
					entriesDeletedMeter.mark(deleted);
					total += deleted;
					remaining -= deleted;
					log.info("removed {} entries for feeds exceeding capacity", total);
				} while (remaining > 0);
			}
		}
		log.info("cleanup done: {} entries for feeds exceeding capacity deleted", total);
	}

	public void cleanStatusesOlderThan(final Date olderThan) {
		log.info("cleaning old read statuses");
		long total = 0;
		long deleted;
		do {
			deleted = unitOfWork.call(() -> feedEntryStatusDAO.deleteOldStatuses(olderThan, batchSize));
			total += deleted;
			log.info("removed {} old read statuses", total);
		} while (deleted != 0);
		log.info("cleanup done: {} old read statuses deleted", total);
	}
}
