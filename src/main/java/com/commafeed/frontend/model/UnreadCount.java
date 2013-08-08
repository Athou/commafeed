package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiClass;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Unread count")
public class UnreadCount implements Serializable {

	private long feedId;
	private long unreadCount;
	private Date newestItemTime;

	public UnreadCount() {

	}

	public UnreadCount(long feedId, long unreadCount, Date newestItemTime) {
		this.feedId = feedId;
		this.unreadCount = unreadCount;
		this.newestItemTime = newestItemTime;
	}

	public long getFeedId() {
		return feedId;
	}

	public void setFeedId(long feedId) {
		this.feedId = feedId;
	}

	public long getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(long unreadCount) {
		this.unreadCount = unreadCount;
	}

	public Date getNewestItemTime() {
		return newestItemTime;
	}

	public void setNewestItemTime(Date newestItemTime) {
		this.newestItemTime = newestItemTime;
	}

}
