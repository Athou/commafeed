package com.commafeed.backend.service.db;

import java.time.Instant;
import java.util.List;

import jakarta.inject.Singleton;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryDAO.FeedCapacity;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.AbstractModel;
import com.commafeed.backend.model.Feed;

import lombok.extern.slf4j.Slf4j;

/**
 * Contains utility methods for cleaning the database
 * 
 */
@Slf4j
@Singleton
public class DatabaseCleaningService {

	private final UnitOfWork unitOfWork;
	private final FeedDAO feedDAO;
	private final FeedEntryDAO feedEntryDAO;
	private final FeedEntryContentDAO feedEntryContentDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final int batchSize;
	private final boolean keepStarredEntries;
	private final Meter entriesDeletedMeter;

	public DatabaseCleaningService(CommaFeedConfiguration config, UnitOfWork unitOfWork, FeedDAO feedDAO, FeedEntryDAO feedEntryDAO,
			FeedEntryContentDAO feedEntryContentDAO, FeedEntryStatusDAO feedEntryStatusDAO, MetricRegistry metrics) {
		this.unitOfWork = unitOfWork;
		this.feedDAO = feedDAO;
		this.feedEntryDAO = feedEntryDAO;
		this.feedEntryContentDAO = feedEntryContentDAO;
		this.feedEntryStatusDAO = feedEntryStatusDAO;
		this.batchSize = config.database().cleanup().batchSize();
		this.keepStarredEntries = config.database().cleanup().keepStarredEntries();
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
					log.debug("removed {} entries for feeds without subscriptions", entriesTotal);
				} while (entriesDeleted > 0);
			}
			deleted = unitOfWork.call(() -> feedDAO.delete(feedDAO.findByIds(feeds.stream().map(AbstractModel::getId).toList())));
			total += deleted;
			log.debug("removed {} feeds without subscriptions", total);
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
			log.debug("removed {} contents without entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} contents without entries deleted", total);
	}

	public void cleanEntriesForFeedsExceedingCapacity(final int maxFeedCapacity) {
		log.info("cleaning entries exceeding feed capacity");
		long total = 0;
		while (true) {
			List<FeedCapacity> feeds = unitOfWork
					.call(() -> feedEntryDAO.findFeedsExceedingCapacity(maxFeedCapacity, batchSize, keepStarredEntries));
			if (feeds.isEmpty()) {
				break;
			}

			for (final FeedCapacity feed : feeds) {
				long remaining = feed.capacity() - maxFeedCapacity;
				int deleted;
				do {
					final long rem = remaining;
					deleted = unitOfWork.call(() -> feedEntryDAO.deleteOldEntries(feed.id(), Math.min(batchSize, rem), keepStarredEntries));
					entriesDeletedMeter.mark(deleted);
					total += deleted;
					remaining -= deleted;
					log.debug("removed {} entries for feeds exceeding capacity", total);
				} while (deleted > 0 && remaining > 0);
			}
		}
		log.info("cleanup done: {} entries for feeds exceeding capacity deleted", total);
	}

	public void cleanEntriesOlderThan(final Instant olderThan) {
		log.info("cleaning old entries");
		long total = 0;
		long deleted;
		do {
			deleted = unitOfWork.call(() -> feedEntryDAO.deleteEntriesOlderThan(olderThan, batchSize, keepStarredEntries));
			entriesDeletedMeter.mark(deleted);
			total += deleted;
			log.debug("removed {} old entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} old entries deleted", total);
	}

	public void cleanStatusesOlderThan(final Instant olderThan) {
		log.info("cleaning old read statuses");
		long total = 0;
		long deleted;
		do {
			deleted = unitOfWork.call(() -> feedEntryStatusDAO.deleteOldStatuses(olderThan, batchSize));
			total += deleted;
			log.debug("removed {} old read statuses", total);
		} while (deleted != 0);
		log.info("cleanup done: {} old read statuses deleted", total);
	}
}
