package com.commafeed.backend.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FEEDENTRYSTATUSES")
@SuppressWarnings("serial")
@Getter
@Setter
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
	private List<FeedEntryTag> tags = new ArrayList<>();

	/**
	 * Denormalization starts here
	 */

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User user;

	@Column
	private Instant entryInserted;

	@Column(name = "entryUpdated")
	private Instant entryPublished;

	/**
	 * The timestamp after which this entry status should be automatically marked as read. Calculated based on the subscription's
	 * autoMarkAsReadAfterDays. Part of the auto-mark-read feature.
	 */
	@Column(name = "auto_mark_as_read_after")
	private Instant autoMarkAsReadAfter;

	public FeedEntryStatus() {

	}

	public FeedEntryStatus(User user, FeedSubscription subscription, FeedEntry entry) {
		this.user = user;
		this.subscription = subscription;
		this.entry = entry;
		this.entryInserted = entry.getInserted();
		this.entryPublished = entry.getPublished();

		/*
		 * Support for the auto-mark-read feature: calculate the expiration timestamp if
		 * a limit is set.
		 */
		if (subscription.getAutoMarkAsReadAfterDays() != null && entry.getPublished() != null) {
			this.autoMarkAsReadAfter = entry.getPublished().plusSeconds(subscription.getAutoMarkAsReadAfterDays() * 24L * 3600L);
		}
	}

}
