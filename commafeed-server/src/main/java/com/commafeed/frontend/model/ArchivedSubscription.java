package com.commafeed.frontend.model;

import java.io.Serializable;
import java.time.Instant;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Archived subscription information")
@Data
@RegisterForReflection
public class ArchivedSubscription implements Serializable {

	@Schema(description = "subscription id", required = true)
	private Long id;

	@Schema(description = "subscription name", required = true)
	private String name;

	@Schema(description = "this subscription's feed url", required = true)
	private String feedUrl;

	@Schema(description = "date the subscription was archived", type = SchemaType.INTEGER)
	private Instant archivedDate;

	@Schema(description = "last time the feed was refreshed", type = SchemaType.INTEGER)
	private Instant lastRefresh;

	@Schema(description = "original category id")
	private String categoryId;

	@Schema(description = "original category name")
	private String categoryName;

	public static ArchivedSubscription build(FeedSubscription subscription) {
		FeedCategory category = subscription.getCategory();
		Feed feed = subscription.getFeed();
		ArchivedSubscription sub = new ArchivedSubscription();
		sub.setId(subscription.getId());
		sub.setName(subscription.getTitle());
		sub.setFeedUrl(feed.getUrl());
		sub.setArchivedDate(subscription.getArchivedDate());
		sub.setLastRefresh(feed.getLastUpdated());
		sub.setCategoryId(category == null ? null : String.valueOf(category.getId()));
		sub.setCategoryName(category == null ? null : category.getName());
		return sub;
	}

}
