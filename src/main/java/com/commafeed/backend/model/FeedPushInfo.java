package com.commafeed.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name = "FEEDPUSHINFOS")
@SuppressWarnings("serial")
public class FeedPushInfo extends AbstractModel {

	@JoinColumn(unique = true)
	@OneToOne(fetch = FetchType.LAZY)
	private Feed feed;

	@Column(length = 2048, nullable = false)
	@Index(name = "topic_index")
	private String topic;

	@Column(length = 2048, nullable = false)
	private String hub;

	private boolean active;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	public String getHub() {
		return hub;
	}

	public void setHub(String hub) {
		this.hub = hub;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
