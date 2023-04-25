package com.commafeed.backend.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false, updatable = false)
	private FeedEntryContent content;

	@Column(length = 2048)
	private String url;

	@Temporal(TemporalType.TIMESTAMP)
	private Date inserted;

	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@OneToMany(mappedBy = "entry", cascade = CascadeType.REMOVE)
	private Set<FeedEntryStatus> statuses;

	@OneToMany(mappedBy = "entry", cascade = CascadeType.REMOVE)
	private Set<FeedEntryTag> tags;

}
