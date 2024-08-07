package com.commafeed.frontend.model;

import java.io.Serializable;
import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Unread count")
@Data
@RegisterForReflection
public class UnreadCount implements Serializable {

	@Schema
	private long feedId;

	@Schema
	private long unreadCount;

	@Schema(type = "number")
	private Instant newestItemTime;

	public UnreadCount() {
	}

	public UnreadCount(long feedId, long unreadCount, Instant newestItemTime) {
		this.feedId = feedId;
		this.unreadCount = unreadCount;
		this.newestItemTime = newestItemTime;
	}

}
