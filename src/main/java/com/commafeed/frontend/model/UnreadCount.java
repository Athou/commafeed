package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel(description = "Unread count")
@Data
public class UnreadCount implements Serializable {

	@ApiModelProperty
	private long feedId;

	@ApiModelProperty
	private long unreadCount;

	@ApiModelProperty(dataType = "number")
	private Date newestItemTime;

	public UnreadCount() {
	}

	public UnreadCount(long feedId, long unreadCount, Date newestItemTime) {
		this.feedId = feedId;
		this.unreadCount = unreadCount;
		this.newestItemTime = newestItemTime;
	}

}
