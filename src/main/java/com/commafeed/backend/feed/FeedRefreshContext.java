package com.commafeed.backend.feed;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;

@Getter
@Setter
public class FeedRefreshContext {
	private Feed feed;
	private List<FeedEntry> entries;
	private boolean urgent;

	public FeedRefreshContext(Feed feed, boolean isUrgent) {
		this.feed = feed;
		this.urgent = isUrgent;
	}
}
