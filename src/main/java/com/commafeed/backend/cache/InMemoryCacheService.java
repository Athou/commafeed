package com.commafeed.backend.cache;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Alternative
@ApplicationScoped
public class InMemoryCacheService extends CacheService {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	private Cache<Long, List<String>> entryCache;

	@PostConstruct
	private void init() {
		int capacity = applicationSettingsService.get().isHeavyLoad() ? 1000000 : 100;
		entryCache = CacheBuilder.newBuilder()
				.maximumSize(capacity).expireAfterWrite(24, TimeUnit.HOURS).build();
	}

	@Override
	public List<String> getLastEntries(Feed feed) {
		List<String> list = entryCache.getIfPresent(feed.getId());
		if (list == null) {
			list = Collections.emptyList();
		}
		return list;
	}

	@Override
	public void setLastEntries(Feed feed, List<String> entries) {
		entryCache.put(feed.getId(), entries);
	}
}
