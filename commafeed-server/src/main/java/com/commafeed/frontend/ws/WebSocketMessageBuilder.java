package com.commafeed.frontend.ws;

import com.commafeed.backend.model.FeedSubscription;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WebSocketMessageBuilder {

	public static String newFeedEntries(FeedSubscription subscription) {
		return String.format("%s:%s", "new-feed-entries", subscription.getId());
	}

}
