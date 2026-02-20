package com.commafeed.backend.model;

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
@Table(name = "FEEDSUBSCRIPTIONS")
@SuppressWarnings("serial")
@Getter
@Setter
public class FeedSubscription extends AbstractModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Feed feed;

	@Column(length = 128, nullable = false)
	private String title;

	@ManyToOne(fetch = FetchType.LAZY)
	private FeedCategory category;

	@OneToMany(mappedBy = "subscription", cascade = CascadeType.REMOVE)
	private Set<FeedEntryStatus> statuses;

	private int position;

	@Column(name = "filtering_expression", length = 4096)
	private String filter;

	@Column(name = "filtering_expression_legacy", length = 4096)
	private String filterLegacy;

	@Column(name = "push_notifications_enabled")
	private boolean pushNotificationsEnabled;

	@Column(name = "auto_mark_as_read_after_days")
	private Integer autoMarkAsReadAfterDays;

}
