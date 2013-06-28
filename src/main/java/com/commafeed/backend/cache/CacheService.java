package com.commafeed.backend.cache;

import java.util.concurrent.TimeUnit;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheService {

	Cache<String, Marker> entryCache = CacheBuilder.newBuilder()
			.maximumSize(1000000).expireAfterWrite(24, TimeUnit.HOURS).build();

	private static enum Marker {
		INSTANCE
	}

	public boolean hasFeedEntry(Feed feed, FeedEntry entry) {
		return entryCache.getIfPresent(buildKey(feed, entry)) == Marker.INSTANCE;
	}

	public void putFeedEntry(Feed feed, FeedEntry entry) {
		entryCache.put(buildKey(feed, entry), Marker.INSTANCE);
	}

	private String buildKey(Feed feed, FeedEntry entry) {
		return String.format("%s:%s:%s", feed.getId(), entry.getGuid(),
				entry.getUrl());
	}
}
