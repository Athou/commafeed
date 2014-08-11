package com.commafeed.backend.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

import com.google.common.collect.Lists;

@Entity
@Table(name = "FEEDENTRYSTATUSES")
@SuppressWarnings("serial")
@Getter
@Setter
@NamedQueries(@NamedQuery(
		name = "Statuses.deleteOld",
		query = "delete from FeedEntryStatus s where s.entryInserted < :date and s.starred = false"))
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

	@Transient
	private List<FeedEntryTag> tags = Lists.newArrayList();

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
