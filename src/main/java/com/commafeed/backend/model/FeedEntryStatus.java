package com.commafeed.backend.model;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "FEEDENTRYSTATUSES")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class FeedEntryStatus extends AbstractModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private FeedSubscription subscription;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private FeedEntry entry;

	@Column(name = "read_status")
	private boolean read;
	private boolean starred;

	/**
	 * Denormalization starts here
	 */

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User user;

	@Temporal(TemporalType.TIMESTAMP)
	private Date entryInserted;

	@Temporal(TemporalType.TIMESTAMP)
	private Date entryUpdated;

	public FeedEntryStatus() {

	}

	public FeedEntryStatus(User user, FeedSubscription subscription, FeedEntry entry) {
		setUser(user);
		setSubscription(subscription);
		setEntry(entry);
		setEntryInserted(entry.getInserted());
		setEntryUpdated(entry.getUpdated());
	}

	public FeedSubscription getSubscription() {
		return subscription;
	}

	public void setSubscription(FeedSubscription subscription) {
		this.subscription = subscription;
	}

	public FeedEntry getEntry() {
		return entry;
	}

	public void setEntry(FeedEntry entry) {
		this.entry = entry;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isStarred() {
		return starred;
	}

	public void setStarred(boolean starred) {
		this.starred = starred;
	}

	public Date getEntryInserted() {
		return entryInserted;
	}

	public void setEntryInserted(Date entryInserted) {
		this.entryInserted = entryInserted;
	}

	public Date getEntryUpdated() {
		return entryUpdated;
	}

	public void setEntryUpdated(Date entryUpdated) {
		this.entryUpdated = entryUpdated;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
