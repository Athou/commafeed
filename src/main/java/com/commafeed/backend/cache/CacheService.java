package com.commafeed.backend.cache;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;

public abstract class CacheService {

	public abstract List<String> getLastEntries(Feed feed);

	public abstract void setLastEntries(Feed feed, List<String> entries);

	public String buildKey(Feed feed, FeedEntry entry) {
		return DigestUtils.sha1Hex(entry.getGuid() +
				entry.getUrl());
	}

}
