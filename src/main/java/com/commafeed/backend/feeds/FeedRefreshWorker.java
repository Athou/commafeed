package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.model.Feed;
import com.google.common.collect.Iterables;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FeedRefreshWorker {

	private static Logger log = LoggerFactory
			.getLogger(FeedRefreshWorker.class);

	private static final ReentrantLock lock = new ReentrantLock();

	@Inject
	FeedFetcher fetcher;

	@Inject
	FeedService feedService;

	@Inject
	FeedEntryService feedEntryService;

	@Resource
	private UserTransaction transaction;

	@Asynchronous
	public void start() {
		while (true) {
			try {
				Feed feed = getNextFeed();
				if (feed != null) {
					log.debug("refreshing " + feed.getUrl());
					update(feed);
				} else {
					log.debug("sleeping");
					Thread.sleep(15000);
				}
			} catch (Exception e) {
				throw new EJBException(e.getMessage(), e);
			}
		}
	}

	private void update(Feed feed) throws NotSupportedException,
			SystemException, SecurityException, IllegalStateException,
			RollbackException, HeuristicMixedException,
			HeuristicRollbackException {

		String message = null;
		int errorCount = 0;
		Date disabledUntil = null;

		Feed fetchedFeed = null;
		try {
			fetchedFeed = fetcher.fetch(feed.getUrl());
		} catch (Exception e) {
			message = "Unable to refresh feed " + feed.getUrl() + " : "
					+ e.getMessage();
			log.info(e.getClass().getName() + " " + message);

			errorCount = feed.getErrorCount() + 1;

			int retriesBeforeDisable = 3;
			if (feed.getErrorCount() >= retriesBeforeDisable) {
				int disabledMinutes = 10 * (feed.getErrorCount()
						- retriesBeforeDisable + 1);
				disabledMinutes = Math.min(60, disabledMinutes);
				disabledUntil = DateUtils.addMinutes(Calendar.getInstance()
						.getTime(), disabledMinutes);
			}
		}

		feed.setMessage(message);
		feed.setErrorCount(errorCount);
		feed.setDisabledUntil(disabledUntil);

		transaction.begin();

		if (fetchedFeed != null) {
			feedEntryService.updateEntries(feed.getUrl(),
					fetchedFeed.getEntries());
			if (feed.getLink() == null) {
				feed.setLink(fetchedFeed.getLink());
			}
		}
		feedService.update(feed);

		transaction.commit();

	}

	private Feed getNextFeed() throws NotSupportedException, SystemException,
			SecurityException, IllegalStateException, RollbackException,
			HeuristicMixedException, HeuristicRollbackException {

		Feed feed = null;
		lock.lock();
		try {
			feed = Iterables.getFirst(feedService.findNextUpdatable(1), null);
			if (feed != null) {
				feed.setLastUpdated(Calendar.getInstance().getTime());
				feedService.update(feed);
			}
		} finally {
			lock.unlock();
		}

		return feed;
	}
}
