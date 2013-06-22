package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("Entry details")
public class Entry implements Serializable {

	public static Entry build(FeedEntryStatus status, String publicUrl,
			boolean proxyImages) {
		Entry entry = new Entry();

		FeedEntry feedEntry = status.getEntry();
		FeedSubscription sub = status.getSubscription();

		entry.setRead(status.isRead());
		entry.setStarred(status.isStarred());

		entry.setId(String.valueOf(feedEntry.getId()));
		entry.setGuid(feedEntry.getGuid());
		entry.setTitle(feedEntry.getContent().getTitle());
		entry.setContent(FeedUtils.proxyImages(feedEntry.getContent()
				.getContent(), publicUrl, proxyImages));
		entry.setRtl(FeedUtils.isRTL(feedEntry));
		entry.setAuthor(feedEntry.getAuthor());
		entry.setEnclosureUrl(feedEntry.getContent().getEnclosureUrl());
		entry.setEnclosureType(feedEntry.getContent().getEnclosureType());
		entry.setDate(feedEntry.getUpdated());
		entry.setInsertedDate(feedEntry.getInserted());
		entry.setUrl(feedEntry.getUrl());

		entry.setFeedName(sub.getTitle());
		entry.setFeedId(String.valueOf(sub.getId()));
		entry.setFeedUrl(sub.getFeed().getUrl());
		entry.setFeedLink(sub.getFeed().getLink());
		entry.setIconUrl(FeedUtils.getFaviconUrl(sub, publicUrl));

		return entry;
	}

	public SyndEntry asRss() {
		SyndEntry entry = new SyndEntryImpl();

		entry.setUri(getGuid());
		entry.setTitle(getTitle());

		SyndContentImpl content = new SyndContentImpl();
		content.setValue(getContent());
		entry.setContents(Arrays.asList(content));
		entry.setLink(getUrl());
		entry.setPublishedDate(getDate());
		return entry;
	}

	@ApiProperty("entry id")
	private String id;

	@ApiProperty("entry guid")
	private String guid;

	@ApiProperty("entry title")
	private String title;

	@ApiProperty("entry content")
	private String content;

	@ApiProperty("wether entry content and title are rtl")
	private boolean rtl;

	@ApiProperty("entry author")
	private String author;

	@ApiProperty("entry enclosure url, if any")
	private String enclosureUrl;

	@ApiProperty("entry enclosure mime type, if any")
	private String enclosureType;

	@ApiProperty("entry publication date")
	private Date date;

	@ApiProperty("entry insertion date in the database")
	private Date insertedDate;

	@ApiProperty("feed id")
	private String feedId;

	@ApiProperty("feed name")
	private String feedName;

	@ApiProperty("this entry's feed url")
	private String feedUrl;

	@ApiProperty("this entry's website url")
	private String feedLink;

	@ApiProperty(value = "The favicon url to use for this feed")
	private String iconUrl;

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

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getFeedLink() {
		return feedLink;
	}

	public void setFeedLink(String feedLink) {
		this.feedLink = feedLink;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public boolean isRtl() {
		return rtl;
	}

	public void setRtl(boolean rtl) {
		this.rtl = rtl;
	}

	public Date getInsertedDate() {
		return insertedDate;
	}

	public void setInsertedDate(Date insertedDate) {
		this.insertedDate = insertedDate;
	}

}
