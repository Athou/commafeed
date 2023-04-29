package com.commafeed.backend.feed;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;

import lombok.extern.slf4j.Slf4j;

/**
 * Calls {@link FeedFetcher} and handles its outcome
 */
@Slf4j
@Singleton
public class FeedRefreshWorker {

	private final FeedRefreshIntervalCalculator refreshIntervalCalculator;
	private final FeedFetcher fetcher;
	private final CommaFeedConfiguration config;
	private final Meter feedFetched;

	@Inject
	public FeedRefreshWorker(FeedRefreshIntervalCalculator refreshIntervalCalculator, FeedFetcher fetcher, CommaFeedConfiguration config,
			MetricRegistry metrics) {
		this.refreshIntervalCalculator = refreshIntervalCalculator;
		this.fetcher = fetcher;
		this.config = config;
		this.feedFetched = metrics.meter(MetricRegistry.name(getClass(), "feedFetched"));

	}

	public FeedAndEntries update(Feed feed) {
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
			feed.setDisabledUntil(refreshIntervalCalculator.onFetchSuccess(fetchedFeed));

			handlePubSub(feed, fetchedFeed.getFeed());

			return new FeedAndEntries(feed, entries);
		} catch (NotModifiedException e) {
			log.debug("Feed not modified : {} - {}", feed.getUrl(), e.getMessage());

			feed.setErrorCount(0);
			feed.setMessage(e.getMessage());
			feed.setDisabledUntil(refreshIntervalCalculator.onFeedNotModified(feed));

			if (e.getNewLastModifiedHeader() != null) {
				feed.setLastModifiedHeader(e.getNewLastModifiedHeader());
			}

			if (e.getNewEtagHeader() != null) {
				feed.setEtagHeader(e.getNewEtagHeader());
			}

			return new FeedAndEntries(feed, Collections.emptyList());
		} catch (Exception e) {
			String message = "Unable to refresh feed " + feed.getUrl() + " : " + e.getMessage();
			log.debug(e.getClass().getName() + " " + message, e);

			feed.setErrorCount(feed.getErrorCount() + 1);
			feed.setMessage(message);
			feed.setDisabledUntil(refreshIntervalCalculator.onFetchError(feed));

			return new FeedAndEntries(feed, Collections.emptyList());
		} finally {
			feedFetched.mark();
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
			} else if (!topic.startsWith("http")) {
				topic = "http://" + topic;
			}
			log.debug("feed {} has pubsub info: {}", feed.getUrl(), topic);
			feed.setPushHub(hub);
			feed.setPushTopic(topic);
			feed.setPushTopicHash(DigestUtils.sha1Hex(topic));
		}
	}

}
