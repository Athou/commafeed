package com.commafeed.backend.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import com.google.common.collect.Sets;

@Entity
@Table(name = "FEEDS")
@SuppressWarnings("serial")
public class Feed extends AbstractModel {

	@Column(length = 2048, nullable = false, unique = true)
	@Index(name = "feed_index")
	private String url;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdated;

	@Column(length = 1024)
	private String message;

	@OneToMany(mappedBy = "feed")
	private Set<FeedEntry> entries = Sets.newHashSet();

	@OneToMany(mappedBy = "feed")
	private Set<FeedSubscription> subscriptions;

	public Feed() {

	}

	public Feed(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Set<FeedEntry> getEntries() {
		return entries;
	}

	public void setEntries(Set<FeedEntry> entries) {
		this.entries = entries;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Set<FeedSubscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<FeedSubscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

}
