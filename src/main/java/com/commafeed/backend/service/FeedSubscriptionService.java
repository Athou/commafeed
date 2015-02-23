package com.commafeed.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feed.FeedQueues;
import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.UnreadCount;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeedSubscriptionService {

	@SuppressWarnings("serial")
	public static class FeedSubscriptionException extends RuntimeException {
		private FeedSubscriptionException(String msg) {
			super(msg);
		}
	}

	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedSubscriptionDAO feedSubscriptionDAO;
	private final FeedService feedService;
	private final FeedQueues queues;
	private final CacheService cache;
	private final CommaFeedConfiguration config;

	public Feed subscribe(User user, String url, String title) {
		return subscribe(user, url, title, null, 0);
	}

	public Feed subscribe(User user, String url, String title, FeedCategory parent) {
		return subscribe(user, url, title, parent, 0);
	}

	public Feed subscribe(User user, String url, String title, FeedCategory category, int position) {

		final String pubUrl = config.getApplicationSettings().getPublicUrl();
		if (StringUtils.isBlank(pubUrl)) {
			throw new FeedSubscriptionException("Public URL of this CommaFeed instance is not set");
		}
		if (url.startsWith(pubUrl)) {
			throw new FeedSubscriptionException("Could not subscribe to a feed from this CommaFeed instance");
		}

		Feed feed = feedService.findOrCreate(url);

		FeedSubscription sub = feedSubscriptionDAO.findByFeed(user, feed);
		if (sub == null) {
			sub = new FeedSubscription();
			sub.setFeed(feed);
			sub.setUser(user);
		}
		sub.setCategory(category);
		sub.setPosition(position);
		sub.setTitle(FeedUtils.truncate(title, 128));
		feedSubscriptionDAO.saveOrUpdate(sub);

		queues.add(feed, false);
		cache.invalidateUserRootCategory(user);
		return feed;
	}

	public boolean unsubscribe(User user, Long subId) {
		FeedSubscription sub = feedSubscriptionDAO.findById(user, subId);
		if (sub != null) {
			feedSubscriptionDAO.delete(sub);
			cache.invalidateUserRootCategory(user);
			return true;
		} else {
			return false;
		}
	}

	public void refreshAll(User user) {
		List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
		for (FeedSubscription sub : subs) {
			Feed feed = sub.getFeed();
			queues.add(feed, true);
		}
	}

	public Map<Long, UnreadCount> getUnreadCount(User user) {
		return feedSubscriptionDAO.findAll(user).stream().collect(Collectors.toMap(s -> s.getId(), s -> getUnreadCount(user, s)));
	}

	private UnreadCount getUnreadCount(User user, FeedSubscription sub) {
		UnreadCount count = cache.getUnreadCount(sub);
		if (count == null) {
			log.debug("unread count cache miss for {}", Models.getId(sub));
			count = feedEntryStatusDAO.getUnreadCount(user, sub);
			cache.setUnreadCount(sub, count);
		}
		return count;
	}

}
