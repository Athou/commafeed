package com.commafeed.frontend.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Subscription implements Serializable {

	private Long id;
	private String name;
	private String message;
	private int errorCount;
	private String feedUrl;
	private long unread;

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

}