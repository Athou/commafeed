package com.commafeed.backend.cache;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.Category;

public abstract class CacheService {

	public abstract List<String> getLastEntries(Feed feed);

	public abstract void setLastEntries(Feed feed, List<String> entries);

	public String buildUniqueEntryKey(Feed feed, FeedEntry entry) {
		return DigestUtils.sha1Hex(entry.getGuid() +
				entry.getUrl());
	}

	public abstract Category getRootCategory(User user);

	public abstract void setRootCategory(User user, Category category);
	
	public abstract Map<Long, Long> getUnreadCounts(User user);

	public abstract void setUnreadCounts(User user, Map<Long, Long> map);

	public abstract void invalidateUserData(User... users);

}
