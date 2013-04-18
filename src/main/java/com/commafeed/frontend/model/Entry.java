package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Entry details")
public class Entry implements Serializable {

	public static Entry build(FeedEntryStatus status) {
		Entry entry = new Entry();

		FeedEntry feedEntry = status.getEntry();
		entry.setId(String.valueOf(status.getId()));
		entry.setTitle(feedEntry.getContent().getTitle());
		entry.setContent(feedEntry.getContent().getContent());
		entry.setEnclosureUrl(status.getEntry().getContent().getEnclosureUrl());
		entry.setEnclosureType(status.getEntry().getContent()
				.getEnclosureType());
		entry.setDate(feedEntry.getUpdated());
		entry.setUrl(feedEntry.getUrl());

		entry.setRead(status.isRead());

		entry.setFeedName(status.getSubscription().getTitle());
		entry.setFeedId(String.valueOf(status.getSubscription().getId()));
		entry.setFeedUrl(status.getSubscription().getFeed().getLink());

		return entry;
	}

	@ApiProperty("entry id")
	private String id;

	@ApiProperty("entry title")
	private String title;

	@ApiProperty("entry content")
	private String content;

	@ApiProperty("entry enclosure url, if any")
	private String enclosureUrl;

	@ApiProperty("entry enclosure mime type, if any")
	private String enclosureType;

	@ApiProperty("entry publication date")
	private Date date;

	@ApiProperty("feed id")
	private String feedId;

	@ApiProperty("feed name")
	private String feedName;

	@ApiProperty("feed url")
	private String feedUrl;

	@ApiProperty("entry url")
	private String url;

	@ApiProperty("read sttaus")
	private boolean read;

	@ApiProperty("starred status")
	private boolean starred;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFeedId() {
		return feedId;
	}

	public void setFeedId(String feedId) {
		this.feedId = feedId;
	}

	public String getFeedName() {
		return feedName;
	}

	public void setFeedName(String feedName) {
		this.feedName = feedName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isStarred() {
		return starred;
	}

	public void setStarred(boolean starred) {
		this.starred = starred;
	}

	public String getFeedUrl() {
		return feedUrl;
	}

	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}

	public String getEnclosureUrl() {
		return enclosureUrl;
	}

	public void setEnclosureUrl(String enclosureUrl) {
		this.enclosureUrl = enclosureUrl;
	}

	public String getEnclosureType() {
		return enclosureType;
	}

	public void setEnclosureType(String enclosureType) {
		this.enclosureType = enclosureType;
	}

}
