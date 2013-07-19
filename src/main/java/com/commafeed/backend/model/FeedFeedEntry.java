package com.commafeed.backend.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "FEED_FEEDENTRIES")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class FeedFeedEntry implements Serializable {

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FEED_ID")
	private Feed feed;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FEEDENTRY_ID")
	private FeedEntry entry;

	@Temporal(TemporalType.TIMESTAMP)
	private Date entryUpdated;

	public FeedFeedEntry() {

	}

	public FeedFeedEntry(Feed feed, FeedEntry entry) {
		this.feed = feed;
		this.entry = entry;
		this.entryUpdated = entry.getUpdated();
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public FeedEntry getEntry() {
		return entry;
	}

	public void setEntry(FeedEntry entry) {
		this.entry = entry;
	}

	public Date getEntryUpdated() {
		return entryUpdated;
	}

	public void setEntryUpdated(Date entryUpdated) {
		this.entryUpdated = entryUpdated;
	}

}
