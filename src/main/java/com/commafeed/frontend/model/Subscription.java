package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("User information")
public class Subscription implements Serializable {

	public static Subscription build(FeedSubscription subscription,
			long unreadCount) {
		FeedCategory category = subscription.getCategory();
		Subscription sub = new Subscription();
		sub.setId(subscription.getId());
		sub.setName(subscription.getTitle());
		sub.setMessage(subscription.getFeed().getMessage());
		sub.setErrorCount(subscription.getFeed().getErrorCount());
		sub.setFeedUrl(subscription.getFeed().getUrl());
		sub.setFeedLink(subscription.getFeed().getLink());
		sub.setLastRefresh(subscription.getFeed().getLastUpdated());
		sub.setUnread(unreadCount);
		sub.setCategoryId(category == null ? null : String.valueOf(category
				.getId()));
		return sub;
	}

	@ApiProperty(value = "subscription id", required = true)
	private Long id;

	@ApiProperty(value = "subscription name", required = true)
	private String name;

	@ApiProperty(value = "error message while fetching the feed", required = true)
	private String message;

	@ApiProperty(value = "error count", required = true)
	private int errorCount;

	@ApiProperty(value = "last time the feed was refreshed", required = true)
	private Date lastRefresh;

	@ApiProperty(value = "this subscription's feed url", required = true)
	private String feedUrl;

	@ApiProperty(value = "this subscription's website url", required = true)
	private String feedLink;

	@ApiProperty(value = "unread count", required = true)
	private long unread;

	@ApiProperty(value = "category id")
	private String categoryId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getUnread() {
		return unread;
	}

	public void setUnread(long unread) {
		this.unread = unread;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFeedUrl() {
		return feedUrl;
	}

	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public String getFeedLink() {
		return feedLink;
	}

	public void setFeedLink(String feedLink) {
		this.feedLink = feedLink;
	}

	public Date getLastRefresh() {
		return lastRefresh;
	}

	public void setLastRefresh(Date lastRefresh) {
		this.lastRefresh = lastRefresh;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

}