package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@ApiModel("User information")
@Data
public class Subscription implements Serializable {

	public static Subscription build(FeedSubscription subscription, String publicUrl, UnreadCount unreadCount) {
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
		sub.setNextRefresh((feed.getDisabledUntil() != null && feed.getDisabledUntil().before(now)) ? null : feed.getDisabledUntil());
		sub.setUnread(unreadCount.getUnreadCount());
		sub.setNewestItemTime(unreadCount.getNewestItemTime());
		sub.setCategoryId(category == null ? null : String.valueOf(category.getId()));
		sub.setFilter(subscription.getFilter());
		return sub;
	}

	@ApiModelProperty(value = "subscription id", required = true)
	private Long id;

	@ApiModelProperty(value = "subscription name", required = true)
	private String name;

	@ApiModelProperty(value = "error message while fetching the feed", required = true)
	private String message;

	@ApiModelProperty(value = "error count", required = true)
	private int errorCount;

	@ApiModelProperty(value = "last time the feed was refreshed", required = true)
	private Date lastRefresh;

	@ApiModelProperty(value = "next time the feed refresh is planned, null if refresh is already queued", required = true)
	private Date nextRefresh;

	@ApiModelProperty(value = "this subscription's feed url", required = true)
	private String feedUrl;

	@ApiModelProperty(value = "this subscription's website url", required = true)
	private String feedLink;

	@ApiModelProperty(value = "The favicon url to use for this feed")
	private String iconUrl;

	@ApiModelProperty(value = "unread count", required = true)
	private long unread;

	@ApiModelProperty(value = "category id")
	private String categoryId;

	@ApiModelProperty("position of the subscription's in the list")
	private Integer position;

	@ApiModelProperty("date of the newest item")
	private Date newestItemTime;

	@ApiModelProperty(value = "JEXL string evaluated on new entries to mark them as read if they do not match")
	private String filter;

}