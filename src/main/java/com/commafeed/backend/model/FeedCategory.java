package com.commafeed.backend.model;

import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "FEEDCATEGORIES")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Data
@EqualsAndHashCode(callSuper = true)
public class FeedCategory extends AbstractModel {

	@Column(length = 128, nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	private FeedCategory parent;

	@OneToMany(mappedBy = "parent")
	private Set<FeedCategory> children;

	@OneToMany(mappedBy = "category")
	private Set<FeedSubscription> subscriptions;

	private boolean collapsed;

	private Integer position;

}
