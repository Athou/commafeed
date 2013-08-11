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
import javax.persistence.Transient;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "FEEDENTRYSTATUSES")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Data
@EqualsAndHashCode(callSuper = true)
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

	@Transient
	private boolean markable;

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

}
