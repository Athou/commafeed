package com.commafeed.backend.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "FEEDENTRIES")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class FeedEntry extends AbstractModel {

	@Column(length = 2048, nullable = false)
	private String guid;

	@Column(length = 40, nullable = false)
	private String guidHash;

	@OneToMany(mappedBy = "entry", cascade = CascadeType.REMOVE)
	private Set<FeedFeedEntry> feedRelationships;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false, updatable = false)
	private FeedEntryContent content;

	@Column(length = 2048)
	private String url;

	@Column(name = "author", length = 128)
	private String author;

	@Temporal(TemporalType.TIMESTAMP)
	private Date inserted;

	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@OneToMany(mappedBy = "entry", cascade = CascadeType.REMOVE)
	private Set<FeedEntryStatus> statuses;

	/**
	 * useful placeholder for the subscription, not persisted
	 */
	@Transient
	private FeedSubscription subscription;

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Set<FeedEntryStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(Set<FeedEntryStatus> statuses) {
		this.statuses = statuses;
	}

	public Date getInserted() {
		return inserted;
	}

	public void setInserted(Date inserted) {
		this.inserted = inserted;
	}

	public FeedEntryContent getContent() {
		return content;
	}

	public void setContent(FeedEntryContent content) {
		this.content = content;
	}

	public String getGuidHash() {
		return guidHash;
	}

	public void setGuidHash(String guidHash) {
		this.guidHash = guidHash;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Set<FeedFeedEntry> getFeedRelationships() {
		return feedRelationships;
	}

	public void setFeedRelationships(Set<FeedFeedEntry> feedRelationships) {
		this.feedRelationships = feedRelationships;
	}

	public FeedSubscription getSubscription() {
		return subscription;
	}

	public void setSubscription(FeedSubscription subscription) {
		this.subscription = subscription;
	}

}
