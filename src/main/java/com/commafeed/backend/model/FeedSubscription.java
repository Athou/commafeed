package com.commafeed.backend.model;

import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "FEEDSUBSCRIPTIONS")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Getter
@Setter
public class FeedSubscription extends AbstractModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	private Feed feed;

	@Column(length = 128, nullable = false)
	private String title;

	@ManyToOne(fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	private FeedCategory category;

	@OneToMany(mappedBy = "subscription", cascade = CascadeType.REMOVE)
	private Set<FeedEntryStatus> statuses;

	private Integer position;

}
