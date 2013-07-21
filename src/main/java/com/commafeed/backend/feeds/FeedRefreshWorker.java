package com.commafeed.backend.feeds;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.MetricsBean;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.feeds.FeedRefreshExecutor.Task;
import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.sun.syndication.io.FeedException;

@ApplicationScoped
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

	private FeedRefreshExecutor pool;

	@PostConstruct
	private void init() {
		ApplicationSettings settings = applicationSettingsService.get();
		int threads = settings.getBackgroundThreads();
		pool = new FeedRefreshExecutor("feed-refresh-worker", threads,
				20 * threads);
	}

	@PreDestroy
	public void shutdown() {
		pool.shutdown();
	}

	public void updateFeed(Feed feed) {
		pool.execute(new FeedTask(feed));
	}

	public int getQueueSize() {
		return pool.getQueueSize();
	}

	public int getActiveCount() {
		return pool.getActiveCount();
	}

	private class FeedTask implements Task {

		private Feed feed;

		public FeedTask(Feed feed) {
			this.feed = feed;
		}

		@Override
		public void run() {
			update(feed);
		}

		@Override
		public boolean isUrgent() {
			return feed.isUrgent();
		}
	}

	private void update(Feed feed) {
		Date now = new Date();
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
			log.debug("Feed not modified : {} - {}", feed.getUrl(),
					e.getMessage());

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
			feed.setPushTopicHash(DigestUtils.sha1Hex(topic));
		}
	}
}
