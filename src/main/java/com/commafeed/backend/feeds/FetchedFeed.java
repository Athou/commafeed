package com.commafeed.backend.feeds;

import java.util.Date;
import java.util.List;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.google.api.client.util.Lists;

public class FetchedFeed {

	private Feed feed = new Feed();
	private List<FeedEntry> entries = Lists.newArrayList();

	private String title;
	private long fetchDuration;
	private Date lastEntryDate;

	/**
	 * pubsubhubbub hub url
	 */
	private String hub;

	/**
	 * pubsubhubbub topic
	 */
	private String topic;

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

	public String getHub() {
		return hub;
	}

	public void setHub(String hub) {
		this.hub = hub;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Date getLastEntryDate() {
		return lastEntryDate;
	}

	public void setLastEntryDate(Date lastEntryDate) {
		this.lastEntryDate = lastEntryDate;
	}

}
