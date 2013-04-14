package com.commafeed.backend.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.google.api.client.util.Sets;

@Entity
@Table(name = "FEEDENTRIES")
@SuppressWarnings("serial")
public class FeedEntry extends AbstractModel {

	@Column(length = 2048, nullable = false)
	private String guid;

	@Column(length = 40, nullable = false)
	@Index(name = "guidHash_index")
	private String guidHash;

	@ManyToMany
	@JoinTable(name = "FEED_FEEDENTRIES", joinColumns = { @JoinColumn(name = "FEED_ID", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "FEEDENTRY_ID", nullable = false, updatable = false) })
	private Set<Feed> feeds = Sets.newHashSet();

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false, updatable = false)
	private FeedEntryContent content;

	@Column(length = 2048)
	private String url;

	@Temporal(TemporalType.TIMESTAMP)
	@Index(name = "inserted_index")
	private Date inserted;

	@Temporal(TemporalType.TIMESTAMP)
	@Index(name = "updated_index")
	private Date updated;

	@OneToMany(mappedBy = "entry")
	private Set<FeedEntryStatus> statuses;

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

	public Set<Feed> getFeeds() {
		return feeds;
	}

	public void setFeeds(Set<Feed> feeds) {
		this.feeds = feeds;
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

}
