package com.commafeed.backend.feeds;

import java.util.Collection;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.services.FeedUpdateService;

@Stateless
public class FeedRefreshUpdater {

	@Inject
	FeedUpdateService feedUpdateService;

	@Inject
	FeedDAO feedDAO;

	@Asynchronous
	public void updateEntries(Feed feed, Collection<FeedEntry> entries) {
		if (CollectionUtils.isNotEmpty(entries)) {
			feedUpdateService.updateEntries(feed, entries);
		}
		feedDAO.update(feed);
	}

}
