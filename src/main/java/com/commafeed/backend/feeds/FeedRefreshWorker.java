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
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedPushInfo;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.FeedPushInfoService;
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
	FeedPushInfoService feedPushInfoService;

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

		FetchedFeed fetchedFeed = null;
		List<FeedEntry> entries = null;

		String message = null;
		int errorCount = 0;
		Date disabledUntil = null;

		try {
			fetchedFeed = fetcher.fetch(feed.getUrl(), false,
					feed.getLastModifiedHeader(), feed.getEtagHeader());
			// stops here if NotModifiedException or any other exception is
			// thrown
			entries = fetchedFeed.getEntries();
			if (applicationSettingsService.get().isHeavyLoad()) {
				disabledUntil = FeedUtils.buildDisabledUntil(
						fetchedFeed.getPublishedDate(), entries);
			}

			feed.setLastUpdateSuccess(Calendar.getInstance().getTime());
			feed.setLink(fetchedFeed.getFeed().getLink());
			feed.setLastModifiedHeader(fetchedFeed.getFeed()
					.getLastModifiedHeader());
			feed.setEtagHeader(fetchedFeed.getFeed().getEtagHeader());

			for (FeedEntry entry : entries) {
				handleEntry(feed, entry);
			}
			handlePubSub(feed, fetchedFeed);

		} catch (NotModifiedException e) {
			log.debug("Feed not modified (304) : " + feed.getUrl());
			List<FeedEntry> feedEntries = feedEntryDAO.findByFeed(feed, 0, 10);
			Date publishedDate = null;
			if (feedEntries.size() > 0) {
				publishedDate = feedEntries.get(0).getInserted();
			}
			feed.setDisabledUntil(FeedUtils.buildDisabledUntil(publishedDate,
					feedEntries));
		} catch (Exception e) {
			message = "Unable to refresh feed " + feed.getUrl() + " : "
					+ e.getMessage();
			if (e instanceof FeedException) {
				log.debug(e.getClass().getName() + " " + message);
			} else {
				log.debug(e.getClass().getName() + " " + message);
			}

			errorCount = feed.getErrorCount() + 1;
			disabledUntil = FeedUtils.buildDisabledUntil(errorCount);
		}

		feed.setErrorCount(errorCount);
		feed.setMessage(message);
		feed.setDisabledUntil(disabledUntil);

		feedRefreshUpdater.updateFeed(feed, entries);

	}

	private void handleEntry(Feed feed, FeedEntry entry) {
		String baseUri = feed.getLink();
		FeedEntryContent content = entry.getContent();
		content.setEnclosureUrl(FeedUtils.truncate(content.getEnclosureUrl(),
				2048));
		content.setContent(FeedUtils.handleContent(content.getContent(),
				baseUri));
		String title = FeedUtils.handleContent(content.getTitle(), baseUri);
		content.setTitle(FeedUtils.truncate(title, 2048));
		entry.setAuthor(FeedUtils.truncate(entry.getAuthor(), 128));
	}

	private void handlePubSub(Feed feed, FetchedFeed fetchedFeed) {
		String hub = fetchedFeed.getHub();
		String topic = fetchedFeed.getTopic();
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
			FeedPushInfo info = feed.getPushInfo();
			if (info == null) {
				info = feedPushInfoService.findOrCreate(feed, hub, topic);
			}
			feed.setPushInfo(info);
		}
	}

	private Feed getNextFeed() {
		return taskGiver.take();
	}

}
