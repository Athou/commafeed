package com.commafeed.backend.services;

import java.util.List;
import java.util.Map;

import javax.ejb.ApplicationException;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.feeds.FeedRefreshTaskGiver;
import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.Models;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.UnreadCount;
import com.google.common.collect.Maps;

@Slf4j
public class FeedSubscriptionService {

	@SuppressWarnings("serial")
	@ApplicationException
	public static class FeedSubscriptionException extends RuntimeException {
		public FeedSubscriptionException(String msg) {
			super(msg);
		}
	}

	@Inject
	FeedService feedService;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Inject
	FeedRefreshTaskGiver taskGiver;

	@Inject
	CacheService cache;

	public Feed subscribe(User user, String url, String title, FeedCategory category) {

		final String pubUrl = applicationSettingsService.get().getPublicUrl();
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
		sub.setPosition(0);
		sub.setTitle(FeedUtils.truncate(title, 128));
		feedSubscriptionDAO.saveOrUpdate(sub);

		taskGiver.add(feed, false);
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
			taskGiver.add(feed, true);
		}
	}

	public UnreadCount getUnreadCount(User user, FeedSubscription sub) {
		UnreadCount count = cache.getUnreadCount(sub);
		if (count == null) {
			log.debug("unread count cache miss for {}", Models.getId(sub));
			count = feedEntryStatusDAO.getUnreadCount(user, sub);
			cache.setUnreadCount(sub, count);
		}
		return count;
	}

	public Map<Long, UnreadCount> getUnreadCount(User user) {
		Map<Long, UnreadCount> map = Maps.newHashMap();
		List<FeedSubscription> subs = feedSubscriptionDAO.findAll(user);
		for (FeedSubscription sub : subs) {
			map.put(sub.getId(), getUnreadCount(user, sub));
		}
		return map;
	}

}
