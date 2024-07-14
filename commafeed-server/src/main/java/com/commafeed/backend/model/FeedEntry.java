package com.commafeed.backend.model;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FEEDENTRIES")
@SuppressWarnings("serial")
@Getter
@Setter
public class FeedEntry extends AbstractModel {

	@Column(length = 2048, nullable = false)
	private String guid;

	@Column(length = 40, nullable = false)
	private String guidHash;

	@ManyToOne(fetch = FetchType.LAZY)
	private Feed feed;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false, updatable = false)
	private FeedEntryContent content;

	@Column(length = 2048)
	private String url;

	/**
	 * the moment the entry was inserted in the database
	 */
	@Column
	private Instant inserted;

	/**
	 * the moment the entry was published in the feed
	 * 
	 */
	@Column(name = "updated")
	private Instant published;

	@OneToMany(mappedBy = "entry", cascade = CascadeType.REMOVE)
	private Set<FeedEntryStatus> statuses;

	@OneToMany(mappedBy = "entry", cascade = CascadeType.REMOVE)
	private Set<FeedEntryTag> tags;

}
