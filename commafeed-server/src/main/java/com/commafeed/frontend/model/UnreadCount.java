package com.commafeed.frontend.model;

import java.io.Serializable;
import java.time.Instant;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.quarkus.runtime.annotations.RegisterForReflection;
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

	@Schema(type = SchemaType.INTEGER)
	private Instant newestItemTime;

	public UnreadCount() {
	}

	public UnreadCount(long feedId, long unreadCount, Instant newestItemTime) {
		this.feedId = feedId;
		this.unreadCount = unreadCount;
		this.newestItemTime = newestItemTime;
	}

}
