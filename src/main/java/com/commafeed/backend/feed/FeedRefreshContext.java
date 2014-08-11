package com.commafeed.backend.feed;

import java.util.List;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;

public class FeedRefreshContext {
	private Feed feed;
	private List<FeedEntry> entries;
	private boolean urgent;

	public FeedRefreshContext(Feed feed, boolean isUrgent) {
		this.feed = feed;
		this.urgent = isUrgent;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public boolean isUrgent() {
		return urgent;
	}

	public void setUrgent(boolean urgent) {
		this.urgent = urgent;
	}

	public List<FeedEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<FeedEntry> entries) {
		this.entries = entries;
	}

}
