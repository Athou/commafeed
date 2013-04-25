package com.commafeed.backend.feeds;

import java.util.Collection;
import java.util.Date;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.google.api.client.util.Lists;

public class FetchedFeed {

	private Feed feed = new Feed();
	private Collection<FeedEntry> entries = Lists.newArrayList();

	private String title;
	private long fetchDuration;
	private Date publishedDate;

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public Collection<FeedEntry> getEntries() {
		return entries;
	}

	public void setEntries(Collection<FeedEntry> entries) {
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

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

}
