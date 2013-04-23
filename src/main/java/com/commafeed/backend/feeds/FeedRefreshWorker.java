package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.feeds.FeedRefreshUpdater.FeedRefreshTask;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;

public class FeedRefreshWorker {

	private static Logger log = LoggerFactory
			.getLogger(FeedRefreshWorker.class);

	@Resource(name = "jms/refreshQueue")
	private Queue queue;

	@Resource
	private ConnectionFactory connectionFactory;

	@Inject
	FeedFetcher fetcher;

	@Inject
	FeedRefreshTaskGiver taskGiver;

	public void start(MutableBoolean running, String threadName) {
		log.info("{} starting", threadName);
		while (running.isTrue()) {
			Feed feed = null;
			try {
				feed = getNextFeed();
				if (feed != null) {
					log.debug("refreshing " + feed.getUrl());
					update(feed);
				} else {
					log.debug("sleeping");
					Thread.sleep(15000);
				}
			} catch (InterruptedException e) {
				log.info(threadName + " interrupted");
				return;
			} catch (Exception e) {
				String feedUrl = "feed is null";
				if (feed != null) {
					feedUrl = feed.getUrl();
				}
				log.error(
						threadName + " (" + feedUrl + ") : " + e.getMessage(),
						e);
			}
		}
	}

	private void update(Feed feed) throws NotSupportedException,
			SystemException, SecurityException, IllegalStateException,
			RollbackException, HeuristicMixedException,
			HeuristicRollbackException, JMSException {

		String message = null;
		int errorCount = 0;
		Date disabledUntil = null;

		Feed fetchedFeed = null;
		boolean modified = true;
		try {
			fetchedFeed = fetcher.fetch(feed.getUrl(), false,
					feed.getLastModifiedHeader(), feed.getEtagHeader());
			if (fetchedFeed.getPublishedDate() != null
					&& feed.getLastUpdateSuccess() != null
					&& fetchedFeed.getPublishedDate().before(
							feed.getLastUpdateSuccess())) {
				throw new NotModifiedException();
			}
			feed.setLastUpdateSuccess(Calendar.getInstance().getTime());
		} catch (NotModifiedException e) {
			modified = false;
			log.debug("Feed not modified (304) : " + feed.getUrl());
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

		if (modified == false && feed.getErrorCount() == 0) {
			// not modified
			return;
		}

		feed.setErrorCount(errorCount);
		feed.setMessage(message);
		feed.setDisabledUntil(disabledUntil);

		Collection<FeedEntry> entries = null;
		if (fetchedFeed != null) {
			feed.setLink(fetchedFeed.getLink());
			feed.setLastModifiedHeader(fetchedFeed.getLastModifiedHeader());
			feed.setEtagHeader(fetchedFeed.getEtagHeader());
			entries = fetchedFeed.getEntries();
		}
		FeedRefreshTask task = new FeedRefreshTask(feed, entries);
		send(task);

	}

	private void send(FeedRefreshTask task) throws JMSException {
		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		MessageProducer producer = session.createProducer(queue);
		producer.setDisableMessageID(true);
		producer.setDisableMessageTimestamp(true);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		ObjectMessage message = session.createObjectMessage(task);
		producer.send(message);
		connection.close();
	}

	private Feed getNextFeed() {
		return taskGiver.take();
	}

}
