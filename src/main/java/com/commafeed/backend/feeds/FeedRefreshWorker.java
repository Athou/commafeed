package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
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

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.services.FeedUpdateService;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FeedRefreshWorker {

	private static Logger log = LoggerFactory
			.getLogger(FeedRefreshWorker.class);

	@Inject
	FeedFetcher fetcher;

	@Inject
	FeedDAO feedDAO;

	@Inject
	FeedUpdateService feedUpdateService;

	@Inject
	FeedRefreshTaskGiver taskGiver;

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
				log.error(e.getMessage(), e);
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
			feedUpdateService.updateEntries(feed.getUrl(),
					fetchedFeed.getEntries());
			if (feed.getLink() == null) {
				feed.setLink(fetchedFeed.getLink());
			}
		}
		feedDAO.update(feed);

		transaction.commit();

	}

	private Feed getNextFeed() {
		return taskGiver.take();
	}
}
