package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.Feed;
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
			String publicUrl, long unreadCount) {
		Date now = new Date();
		FeedCategory category = subscription.getCategory();
		Feed feed = subscription.getFeed();
		Subscription sub = new Subscription();
		sub.setId(subscription.getId());
		sub.setName(subscription.getTitle());
		sub.setPosition(subscription.getPosition());
		sub.setMessage(feed.getMessage());
		sub.setErrorCount(feed.getErrorCount());
		sub.setFeedUrl(feed.getUrl());
		sub.setFeedLink(feed.getLink());
		sub.setIconUrl(FeedUtils.getFaviconUrl(subscription, publicUrl));
		sub.setLastRefresh(feed.getLastUpdated());
		sub.setNextRefresh((feed.getDisabledUntil() != null && feed
				.getDisabledUntil().before(now)) ? null : feed
				.getDisabledUntil());
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

	@ApiProperty(value = "next time the feed refresh is planned, null if refresh is already queued", required = true)
	private Date nextRefresh;

	@ApiProperty(value = "this subscription's feed url", required = true)
	private String feedUrl;

	@ApiProperty(value = "this subscription's website url", required = true)
	private String feedLink;

	@ApiProperty(value = "The favicon url to use for this feed")
	private String iconUrl;

	@ApiProperty(value = "unread count", required = true)
	private long unread;

	@ApiProperty(value = "category id")
	private String categoryId;

	@ApiProperty("position of the subscription's in the list")
	private Integer position;

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

	public Date getNextRefresh() {
		return nextRefresh;
	}

	public void setNextRefresh(Date nextRefresh) {
		this.nextRefresh = nextRefresh;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

}