package com.commafeed.backend.cache;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;

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
	public Long getUnreadCount(FeedSubscription sub) {
		return null;
	}

	@Override
	public void setUnreadCount(FeedSubscription sub, Long count) {

	}

	@Override
	public List<FeedCategory> getUserCategories(User user) {
		return null;
	}

	@Override
	public void setUserCategories(User user, List<FeedCategory> categories) {

	}

	@Override
	public void invalidateUserCategories(User user) {

	}

	@Override
	public List<FeedSubscription> getUserSubscriptions(User user) {
		return null;
	}

	@Override
	public void setUserSubscriptions(User user, List<FeedSubscription> subs) {

	}

	@Override
	public void invalidateUserSubscriptions(User user) {

	}

	@Override
	public void invalidateUnreadCount(FeedSubscription... subs) {

	}

}
