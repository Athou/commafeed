package com.commafeed.frontend.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("User information")
public class Subscription implements Serializable {

	@ApiProperty(value = "subscription id", required = true)
	private Long id;

	@ApiProperty(value = "subscription name", required = true)
	private String name;

	@ApiProperty(value = "error message while fetching the feed", required = true)
	private String message;

	@ApiProperty(value = "error count", required = true)
	private int errorCount;

	@ApiProperty(value = "this subscription's feed url", required = true)
	private String feedUrl;

	@ApiProperty(value = "unread count", required = true)
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