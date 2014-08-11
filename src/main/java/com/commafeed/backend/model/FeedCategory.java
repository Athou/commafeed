package com.commafeed.backend.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FEEDCATEGORIES")
@SuppressWarnings("serial")
@Getter
@Setter
public class FeedCategory extends AbstractModel {

	@Column(length = 128, nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	private FeedCategory parent;

	@OneToMany(mappedBy = "parent")
	private Set<FeedCategory> children;

	@OneToMany(mappedBy = "category")
	private Set<FeedSubscription> subscriptions;

	private boolean collapsed;

	private Integer position;

}
