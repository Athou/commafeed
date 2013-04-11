package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedEntryService;
import com.commafeed.backend.dao.FeedService;
import com.commafeed.backend.model.Feed;

@Stateless
public class FeedUpdater {

	private static Logger log = LoggerFactory.getLogger(FeedTimer.class);

	@Inject
	FeedFetcher fetcher;

	@Inject
	FeedService feedService;

	@Inject
	FeedEntryService feedEntryService;

	@Asynchronous
	@Lock(LockType.READ)
	@AccessTimeout(value = 4, unit = TimeUnit.SECONDS)
	public void update(Feed feed) {

		try {

			Date now = Calendar.getInstance().getTime();
			Date disabledUntil = feed.getDisabledUntil();

			if (disabledUntil == null || disabledUntil.before(now)) {
				Feed fetchedFeed = fetcher.fetch(feed.getUrl());
				if (feed.getLink() == null) {
					feed.setLink(fetchedFeed.getLink());
					feedService.update(feed);
				}
				feedEntryService.updateEntries(feed.getUrl(),
						fetchedFeed.getEntries());

				feed.setMessage(null);
				feed.setErrorCount(0);
				feed.setDisabledUntil(null);
			}
		} catch (Exception e) {
			String message = "Unable to refresh feed " + feed.getUrl() + " : "
					+ e.getMessage();
			log.info(e.getClass().getName() + " " + message);
			feed.setMessage(message);
			feed.setErrorCount(feed.getErrorCount() + 1);

			int retriesBeforeDisable = 3;

			if (feed.getErrorCount() >= retriesBeforeDisable) {
				int disabledMinutes = 10 * (feed.getErrorCount()
						- retriesBeforeDisable + 1);
				disabledMinutes = Math.min(60, disabledMinutes);
				feed.setDisabledUntil(DateUtils.addMinutes(Calendar
						.getInstance().getTime(), disabledMinutes));
			}
		} finally {
			feed.setLastUpdated(Calendar.getInstance().getTime());
			feedService.update(feed);
		}
	}
}
