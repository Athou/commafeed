package com.commafeed.backend.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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

	@ManyToMany
	@JoinTable(name = "FEED_FEEDENTRIES", joinColumns = { @JoinColumn(name = "FEED_ID", nullable = false, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "FEEDENTRY_ID", nullable = false, updatable = false) })
	private Set<Feed> feeds = Sets.newHashSet();

	@Column(length = 2048)
	private String title;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	private String content;

	@Column(length = 2048)
	private String url;

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

}
