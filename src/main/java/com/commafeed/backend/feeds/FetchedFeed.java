package com.commafeed.backend.feeds;

import java.util.List;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.google.common.collect.Lists;

public class FetchedFeed {

	private Feed feed = new Feed();
	private List<FeedEntry> entries = Lists.newArrayList();

	private String title;
	private String urlAfterRedirect;
	private long fetchDuration;

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public List<FeedEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<FeedEntry> entries) {
		this.entries = entries;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getFetchDuration() {
		return fetchDuration;
	}

	public void setFetchDuration(long fetchDuration) {
		this.fetchDuration = fetchDuration;
	}

	public String getUrlAfterRedirect() {
		return urlAfterRedirect;
	}

	public void setUrlAfterRedirect(String urlAfterRedirect) {
		this.urlAfterRedirect = urlAfterRedirect;
	}

}
