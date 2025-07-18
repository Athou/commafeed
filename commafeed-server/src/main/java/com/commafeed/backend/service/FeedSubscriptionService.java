package com.commafeed.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Singleton;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feed.FeedRefreshEngine;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.UnreadCount;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class FeedSubscriptionService {

	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedService feedService;
	private final FeedRefreshEngine feedRefreshEngine;
	private final CommaFeedConfiguration config;

	public FeedSubscriptionService(FeedEntryStatusDAO feedEntryStatusDAO, FeedSubscriptionDAO feedSubscriptionDAO, FeedService feedService,
			FeedRefreshEngine feedRefreshEngine, CommaFeedConfiguration config) {
		this.feedEntryStatusDAO = feedEntryStatusDAO;
		this.feedSubscriptionDAO = feedSubscriptionDAO;
		this.feedService = feedService;
		this.feedRefreshEngine = feedRefreshEngine;
		this.config = config;

		// automatically refresh new feeds after they are subscribed to
		// we need to use this hook because the feed needs to have been persisted before being processed by the feed engine
		feedSubscriptionDAO.onPostCommitInsert(sub -> {
			Feed feed = sub.getFeed();
			if (feed.getDisabledUntil() == null || feed.getDisabledUntil().isBefore(Instant.now())) {
				feedRefreshEngine.refreshImmediately(feed);
			}
		});
	}

	public long subscribe(User user, String url, String title) {
		return subscribe(user, url, title, null, 0);
	}

	public long subscribe(User user, String url, String title, FeedCategory parent) {
		return subscribe(user, url, title, parent, 0);
	}

	public long subscribe(User user, String url, String title, FeedCategory category, int position) {
		Integer maxFeedsPerUser = config.database().cleanup().maxFeedsPerUser();
		if (maxFeedsPerUser > 0 && feedSubscriptionDAO.count(user) >= maxFeedsPerUser) {
			String message = String.format("You cannot subscribe to more feeds on this CommaFeed instance (max %s feeds per user)",
					maxFeedsPerUser);
			throw new FeedSubscriptionException(message);
		}

		Feed feed = feedService.findOrCreate(url);

		// upgrade feed to https if it was using http
		if (FeedUtils.isHttp(feed.getUrl()) && FeedUtils.isHttps(url)) {
			feed.setUrl(url);
		}

		FeedSubscription sub = feedSubscriptionDAO.findByFeed(user, feed);
		if (sub == null) {
			sub = new FeedSubscription();
			sub.setFeed(feed);
			sub.setUser(user);
		}
		sub.setCategory(category);
		sub.setPosition(position);
		sub.setTitle(FeedUtils.truncate(title, 128));
		return feedSubscriptionDAO.merge(sub).getId();
	}

	public boolean unsubscribe(User user, Long subId) {
		FeedSubscription sub = feedSubscriptionDAO.findById(user, subId);
		if (sub != null) {
			feedSubscriptionDAO.delete(sub);
			return true;
		} else {
			return false;
		}
	}

	public void refreshAll(User user) throws ForceFeedRefreshTooSoonException {
		Instant lastForceRefresh = user.getLastForceRefresh();
		if (lastForceRefresh != null && lastForceRefresh.plus(config.feedRefresh().forceRefreshCooldownDuration()).isAfter(Instant.now())) {
			throw new ForceFeedRefreshTooSoonException();
		}

		List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
		for (FeedSubscription sub : subs) {
			Feed feed = sub.getFeed();
			feedRefreshEngine.refreshImmediately(feed);
		}

		user.setLastForceRefresh(Instant.now());
	}

	public Map<Long, UnreadCount> getUnreadCount(User user) {
		return feedSubscriptionDAO.findAll(user)
				.stream()
				.collect(Collectors.toMap(FeedSubscription::getId, feedEntryStatusDAO::getUnreadCount));
	}

	@SuppressWarnings("serial")
	public static class FeedSubscriptionException extends RuntimeException {
		private FeedSubscriptionException(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public static class ForceFeedRefreshTooSoonException extends Exception {
		private ForceFeedRefreshTooSoonException() {
			super();
		}
	}

}
