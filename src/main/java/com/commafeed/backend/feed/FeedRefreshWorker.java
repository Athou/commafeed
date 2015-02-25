package com.commafeed.backend.feed;

import io.dropwizard.lifecycle.Managed;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.feed.FeedRefreshExecutor.Task;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;

/**
 * Calls {@link FeedFetcher} and handles its outcome
 * 
 */
@Slf4j
@Singleton
public class FeedRefreshWorker implements Managed {

	private final FeedRefreshUpdater feedRefreshUpdater;
	private final FeedFetcher fetcher;
	private final FeedQueues queues;
	private final CommaFeedConfiguration config;
	private final FeedRefreshExecutor pool;

	@Inject
	public FeedRefreshWorker(FeedRefreshUpdater feedRefreshUpdater, FeedFetcher fetcher, FeedQueues queues, CommaFeedConfiguration config,
			MetricRegistry metrics) {
		this.feedRefreshUpdater = feedRefreshUpdater;
		this.fetcher = fetcher;
		this.config = config;
		this.queues = queues;
		int threads = config.getApplicationSettings().getBackgroundThreads();
		pool = new FeedRefreshExecutor("feed-refresh-worker", threads, Math.min(20 * threads, 1000), metrics);
	}

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {
		pool.shutdown();
	}

	public void updateFeed(FeedRefreshContext context) {
		pool.execute(new FeedTask(context));
	}

	private class FeedTask implements Task {

		private FeedRefreshContext context;

		public FeedTask(FeedRefreshContext context) {
			this.context = context;
		}

		@Override
		public void run() {
			update(context);
		}

		@Override
		public boolean isUrgent() {
			return context.isUrgent();
		}
	}

	private void update(FeedRefreshContext context) {
		Feed feed = context.getFeed();
		int refreshInterval = config.getApplicationSettings().getRefreshIntervalMinutes();
		Date disabledUntil = DateUtils.addMinutes(new Date(), refreshInterval);
		try {
			String url = Optional.ofNullable(feed.getUrlAfterRedirect()).orElse(feed.getUrl());
			FetchedFeed fetchedFeed = fetcher.fetch(url, false, feed.getLastModifiedHeader(), feed.getEtagHeader(),
					feed.getLastPublishedDate(), feed.getLastContentHash());
			// stops here if NotModifiedException or any other exception is thrown
			List<FeedEntry> entries = fetchedFeed.getEntries();

			Integer maxFeedCapacity = config.getApplicationSettings().getMaxFeedCapacity();
			if (maxFeedCapacity > 0) {
				entries = entries.stream().limit(maxFeedCapacity).collect(Collectors.toList());
			}

			if (config.getApplicationSettings().getHeavyLoad()) {
				disabledUntil = FeedUtils.buildDisabledUntil(fetchedFeed.getFeed().getLastEntryDate(), fetchedFeed.getFeed()
						.getAverageEntryInterval(), disabledUntil);
			}
			String urlAfterRedirect = fetchedFeed.getUrlAfterRedirect();
			if (StringUtils.equals(url, urlAfterRedirect)) {
				urlAfterRedirect = null;
			}
			feed.setUrlAfterRedirect(urlAfterRedirect);
			feed.setLink(fetchedFeed.getFeed().getLink());
			feed.setLastModifiedHeader(fetchedFeed.getFeed().getLastModifiedHeader());
			feed.setEtagHeader(fetchedFeed.getFeed().getEtagHeader());
			feed.setLastContentHash(fetchedFeed.getFeed().getLastContentHash());
			feed.setLastPublishedDate(fetchedFeed.getFeed().getLastPublishedDate());
			feed.setAverageEntryInterval(fetchedFeed.getFeed().getAverageEntryInterval());
			feed.setLastEntryDate(fetchedFeed.getFeed().getLastEntryDate());

			feed.setErrorCount(0);
			feed.setMessage(null);
			feed.setDisabledUntil(disabledUntil);

			handlePubSub(feed, fetchedFeed.getFeed());
			context.setEntries(entries);
			feedRefreshUpdater.updateFeed(context);

		} catch (NotModifiedException e) {
			log.debug("Feed not modified : {} - {}", feed.getUrl(), e.getMessage());

			if (config.getApplicationSettings().getHeavyLoad()) {
				disabledUntil = FeedUtils.buildDisabledUntil(feed.getLastEntryDate(), feed.getAverageEntryInterval(), disabledUntil);
			}
			feed.setErrorCount(0);
			feed.setMessage(e.getMessage());
			feed.setDisabledUntil(disabledUntil);

			queues.giveBack(feed);
		} catch (Exception e) {
			String message = "Unable to refresh feed " + feed.getUrl() + " : " + e.getMessage();
			log.debug(e.getClass().getName() + " " + message, e);

			feed.setErrorCount(feed.getErrorCount() + 1);
			feed.setMessage(message);
			feed.setDisabledUntil(FeedUtils.buildDisabledUntil(feed.getErrorCount()));

			queues.giveBack(feed);
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
