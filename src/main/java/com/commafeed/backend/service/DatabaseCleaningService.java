package com.commafeed.backend.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.SessionFactory;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntryStatus;

/**
 * Contains utility methods for cleaning the database
 * 
 */
@Slf4j
@RequiredArgsConstructor
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
		do {
			deleted = new UnitOfWork<Integer>(sessionFactory) {
				@Override
				protected Integer runInSession() throws Exception {
					List<Feed> feeds = feedDAO.findWithoutSubscriptions(BATCH_SIZE);
					return feedDAO.delete(feeds);
				};
			}.run();
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
			deleted = new UnitOfWork<Integer>(sessionFactory) {
				@Override
				protected Integer runInSession() throws Exception {
					return feedEntryContentDAO.deleteWithoutEntries(BATCH_SIZE);
				}
			}.run();
			total += deleted;
			log.info("removed {} contents without entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} contents without entries deleted", total);
		return total;
	}

	public long cleanEntriesOlderThan(long value, TimeUnit unit) {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1 * (int) unit.toMinutes(value));

		long total = 0;
		int deleted = 0;
		do {
			deleted = new UnitOfWork<Integer>(sessionFactory) {
				@Override
				protected Integer runInSession() throws Exception {
					return feedEntryDAO.delete(cal.getTime(), BATCH_SIZE);
				}
			}.run();
			total += deleted;
			log.info("removed {} entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} entries deleted", total);
		return total;
	}

	public long cleanStatusesOlderThan(final Date olderThan) {
		log.info("cleaning old read statuses");
		long total = 0;
		int deleted = 0;
		do {
			deleted = new UnitOfWork<Integer>(sessionFactory) {
				@Override
				protected Integer runInSession() throws Exception {
					List<FeedEntryStatus> list = feedEntryStatusDAO.getOldStatuses(olderThan, BATCH_SIZE);
					return feedEntryStatusDAO.delete(list);
				}
			}.run();
			total += deleted;
			log.info("cleaned {} old read statuses", total);
		} while (deleted != 0);
		log.info("cleanup done: {} old read statuses deleted", total);
		return total;
	}
}
