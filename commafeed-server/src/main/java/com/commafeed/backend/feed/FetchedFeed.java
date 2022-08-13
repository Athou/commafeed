package com.commafeed.backend.feed;

import java.util.ArrayList;
import java.util.List;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FetchedFeed {

	private Feed feed = new Feed();
	private List<FeedEntry> entries = new ArrayList<>();

	private String title;
	private String urlAfterRedirect;
	private long fetchDuration;

}
