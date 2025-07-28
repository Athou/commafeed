package com.commafeed.frontend.model;

import java.io.Serializable;
import java.time.Instant;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "User information")
@Data
@RegisterForReflection
public class Subscription implements Serializable {

	@Schema(description = "subscription id", required = true)
	private Long id;

	@Schema(description = "subscription name", required = true)
	private String name;

	@Schema(description = "error message while fetching the feed")
	private String message;

	@Schema(description = "error count", required = true)
	private int errorCount;

	@Schema(description = "last time the feed was refreshed", type = SchemaType.INTEGER)
	private Instant lastRefresh;

	@Schema(description = "next time the feed refresh is planned, null if refresh is already queued", type = SchemaType.INTEGER)
	private Instant nextRefresh;

	@Schema(description = "this subscription's feed url", required = true)
	private String feedUrl;

	@Schema(description = "this subscription's website url", required = true)
	private String feedLink;

	@Schema(description = "The favicon url to use for this feed", required = true)
	private String iconUrl;

	@Schema(description = "unread count", required = true)
	private long unread;

	@Schema(description = "category id")
	private String categoryId;

	@Schema(description = "position of the subscription's in the list")
	private int position;

	@Schema(description = "date of the newest item", type = SchemaType.INTEGER)
	private Instant newestItemTime;

	@Schema(description = "JEXL string evaluated on new entries to mark them as read if they do not match")
	private String filter;

	public static Subscription build(FeedSubscription subscription, UnreadCount unreadCount) {
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
		sub.setIconUrl(FeedUtils.getFaviconUrl(subscription));
		sub.setLastRefresh(feed.getLastUpdated());
		sub.setNextRefresh(
				(feed.getDisabledUntil() != null && feed.getDisabledUntil().isBefore(Instant.now())) ? null : feed.getDisabledUntil());
		sub.setUnread(unreadCount.getUnreadCount());
		sub.setNewestItemTime(unreadCount.getNewestItemTime());
		sub.setCategoryId(category == null ? null : String.valueOf(category.getId()));
		sub.setFilter(subscription.getFilter());
		return sub;
	}

}