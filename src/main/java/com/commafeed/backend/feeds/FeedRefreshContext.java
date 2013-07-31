package com.commafeed.backend.feeds;

import java.util.List;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;

public class FeedRefreshContext {
	private Feed feed;
	private List<FeedEntry> entries;
	private boolean isUrgent;

	public FeedRefreshContext(Feed feed, boolean isUrgent) {
		this.feed = feed;
		this.isUrgent = isUrgent;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public boolean isUrgent() {
		return isUrgent;
	}

	public void setUrgent(boolean isUrgent) {
		this.isUrgent = isUrgent;
	}

	public List<FeedEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<FeedEntry> entries) {
		this.entries = entries;
	}

}
