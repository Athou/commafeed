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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Sets;

@Entity
@Table(name = "FEEDCATEGORIES")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public FeedCategory getParent() {
		return parent;
	}

	public void setParent(FeedCategory parent) {
		this.parent = parent;
	}

	public Set<FeedSubscription> getSubscriptions() {
		if (subscriptions == null) {
			return Sets.newHashSet();
		}
		return subscriptions;
	}

	public void setSubscriptions(Set<FeedSubscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public Set<FeedCategory> getChildren() {
		return children;
	}

	public void setChildren(Set<FeedCategory> children) {
		this.children = children;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

}
