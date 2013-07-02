package com.commafeed.backend.cache;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.Category;

@Alternative
@ApplicationScoped
public class NoopCacheService extends CacheService {

	@Override
	public List<String> getLastEntries(Feed feed) {
		return Collections.emptyList();
	}

	@Override
	public void setLastEntries(Feed feed, List<String> entries) {
	}

	@Override
	public Category getRootCategory(User user) {
		return null;
	}

	@Override
	public void setRootCategory(User user, Category category) {

	}

	@Override
	public void invalidateRootCategory(User... users) {

	}
}
