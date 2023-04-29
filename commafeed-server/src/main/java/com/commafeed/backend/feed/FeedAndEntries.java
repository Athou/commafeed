package com.commafeed.backend.feed;

import java.util.List;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;

import lombok.Value;

@Value
public class FeedAndEntries {
	Feed feed;
	List<FeedEntry> entries;
}
