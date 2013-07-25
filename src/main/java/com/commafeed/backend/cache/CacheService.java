package com.commafeed.backend.cache;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.Category;

public abstract class CacheService {

	// feed entries for faster refresh
	public abstract List<String> getLastEntries(Feed feed);

	public abstract void setLastEntries(Feed feed, List<String> entries);

	public String buildUniqueEntryKey(Feed feed, FeedEntry entry) {
		return DigestUtils.sha1Hex(entry.getGuid() + entry.getUrl());
	}

	// user categories
	public abstract Category getUserRootCategory(User user);

	public abstract void setUserRootCategory(User user, Category category);

	public abstract void invalidateUserRootCategory(User... users);

	// unread count
	public abstract Long getUnreadCount(FeedSubscription sub);

	public abstract void setUnreadCount(FeedSubscription sub, Long count);

	public abstract void invalidateUnreadCount(FeedSubscription... subs);

}
