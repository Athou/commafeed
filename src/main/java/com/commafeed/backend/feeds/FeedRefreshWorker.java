package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.sun.syndication.io.FeedException;

public class FeedRefreshWorker {

	private static Logger log = LoggerFactory
			.getLogger(FeedRefreshWorker.class);

	@Inject
	FeedRefreshUpdater feedRefreshUpdater;

	@Inject
	FeedFetcher fetcher;

	@Inject
	FeedRefreshTaskGiver taskGiver;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	MetricsBean metricsBean;

	@Inject
	FeedEntryDAO feedEntryDAO;

	public void start(MutableBoolean running, String threadName) {
		log.info("{} starting", threadName);

		try {
			// sleeping before starting, let everything settle
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error(threadName + e.getMessage(), e);
		}

		while (running.isTrue()) {
			Feed feed = null;
			try {
				feed = getNextFeed();
				if (feed != null) {
					log.debug("refreshing " + feed.getUrl());
					update(feed);
				} else {
					log.debug("sleeping");
					metricsBean.threadWaited();
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

	private void update(Feed feed) {
		Date now = Calendar.getInstance().getTime();
		try {
			FetchedFeed fetchedFeed = fetcher.fetch(feed.getUrl(), false,
					feed.getLastModifiedHeader(), feed.getEtagHeader(),
					feed.getLastPublishedDate(), feed.getLastContentHash());
			// stops here if NotModifiedException or any other exception is
			// thrown
			List<FeedEntry> entries = fetchedFeed.getEntries();

			Date disabledUntil = null;
			if (applicationSettingsService.get().isHeavyLoad()) {
				disabledUntil = FeedUtils.buildDisabledUntil(fetchedFeed
						.getFeed().getLastEntryDate(), fetchedFeed.getFeed()
						.getAverageEntryInterval());
			}

			feed.setLastUpdateSuccess(now);
			feed.setLink(fetchedFeed.getFeed().getLink());
			feed.setLastModifiedHeader(fetchedFeed.getFeed()
					.getLastModifiedHeader());
			feed.setEtagHeader(fetchedFeed.getFeed().getEtagHeader());
			feed.setLastContentHash(fetchedFeed.getFeed().getLastContentHash());
			feed.setLastPublishedDate(fetchedFeed.getFeed()
					.getLastPublishedDate());
			feed.setAverageEntryInterval(fetchedFeed.getFeed()
					.getAverageEntryInterval());
			feed.setLastEntryDate(fetchedFeed.getFeed().getLastEntryDate());

			feed.setErrorCount(0);
			feed.setMessage(null);
			feed.setDisabledUntil(disabledUntil);

			handlePubSub(feed, fetchedFeed.getFeed());
			feedRefreshUpdater.updateFeed(feed, entries);

		} catch (NotModifiedException e) {
			log.debug("Feed not modified (304) : " + feed.getUrl());

			Date disabledUntil = null;
			if (applicationSettingsService.get().isHeavyLoad()) {
				disabledUntil = FeedUtils
						.buildDisabledUntil(feed.getLastEntryDate(),
								feed.getAverageEntryInterval());
			}
			feed.setErrorCount(0);
			feed.setMessage(null);
			feed.setDisabledUntil(disabledUntil);

			taskGiver.giveBack(feed);
		} catch (Exception e) {
			String message = "Unable to refresh feed " + feed.getUrl() + " : "
					+ e.getMessage();
			if (e instanceof FeedException) {
				log.debug(e.getClass().getName() + " " + message, e);
			} else {
				log.debug(e.getClass().getName() + " " + message, e);
			}

			feed.setErrorCount(feed.getErrorCount() + 1);
			feed.setMessage(message);
			feed.setDisabledUntil(FeedUtils.buildDisabledUntil(feed
					.getErrorCount()));

			taskGiver.giveBack(feed);
		}
	}

	private void handlePubSub(Feed feed, Feed fetchedFeed) {
		String hub = fetchedFeed.getPushHub();
		String topic = fetchedFeed.getPushTopic();
		if (hub != null && topic != null) {
			if (hub.contains("hubbub.api.typepad.com")) {
				// that hub does not exist anymore
				return;
			}
			if (topic.startsWith("www.")) {
				topic = "http://" + topic;
			} else if (topic.startsWith("feed://")) {
				topic = "http://" + topic.substring(7);
			} else if (topic.startsWith("http") == false) {
				topic = "http://" + topic;
			}
			log.debug("feed {} has pubsub info: {}", feed.getUrl(), topic);
			feed.setPushHub(hub);
			feed.setPushTopic(topic);
		}
	}

	private Feed getNextFeed() {
		return taskGiver.take();
	}

}
