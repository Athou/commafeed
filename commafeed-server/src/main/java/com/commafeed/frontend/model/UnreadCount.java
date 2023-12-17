package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Unread count")
@Data
public class UnreadCount implements Serializable {

	@Schema
	private long feedId;

	@Schema
	private long unreadCount;

	@Schema(type = "number")
	private Date newestItemTime;

	public UnreadCount() {
	}

	public UnreadCount(long feedId, long unreadCount, Date newestItemTime) {
		this.feedId = feedId;
		this.unreadCount = unreadCount;
		this.newestItemTime = newestItemTime;
	}

}
