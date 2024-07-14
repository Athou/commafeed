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

	public FeedEntryStatus() {

	}

	public FeedEntryStatus(User user, FeedSubscription subscription, FeedEntry entry) {
		this.user = user;
		this.subscription = subscription;
		this.entry = entry;
		this.entryInserted = entry.getInserted();
		this.entryPublished = entry.getPublished();
	}

}
