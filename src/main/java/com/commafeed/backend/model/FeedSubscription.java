package com.commafeed.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "FEEDSUBSCRIPTIONS")
@SuppressWarnings("serial")
public class FeedSubscription extends AbstractModel {

	@ManyToOne
	private User user;

	@ManyToOne
	private Feed feed;

	@Column(length = 128)
	private String title;

	@ManyToOne
	private FeedCategory category;

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public FeedCategory getCategory() {
		return category;
	}

	public void setCategory(FeedCategory category) {
		this.category = category;
	}

}
