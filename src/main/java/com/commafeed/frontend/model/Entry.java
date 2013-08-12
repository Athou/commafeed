package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import lombok.Data;

import com.commafeed.backend.feeds.FeedUtils;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("Entry details")
@Data
public class Entry implements Serializable {

	public static Entry build(FeedEntryStatus status, String publicUrl, boolean proxyImages) {
		Entry entry = new Entry();

		FeedEntry feedEntry = status.getEntry();
		FeedSubscription sub = status.getSubscription();
		FeedEntryContent content = feedEntry.getContent();

		entry.setId(String.valueOf(feedEntry.getId()));
		entry.setGuid(feedEntry.getGuid());
		entry.setRead(status.isRead());
		entry.setStarred(status.isStarred());
		entry.setMarkable(status.isMarkable());
		entry.setDate(feedEntry.getUpdated());
		entry.setInsertedDate(feedEntry.getInserted());
		entry.setUrl(feedEntry.getUrl());
		entry.setFeedName(sub.getTitle());
		entry.setFeedId(String.valueOf(sub.getId()));
		entry.setFeedUrl(sub.getFeed().getUrl());
		entry.setFeedLink(sub.getFeed().getLink());
		entry.setIconUrl(FeedUtils.getFaviconUrl(sub, publicUrl));

		if (content != null) {
			entry.setRtl(FeedUtils.isRTL(feedEntry));
			entry.setTitle(content.getTitle());
			entry.setContent(FeedUtils.proxyImages(content.getContent(), publicUrl, proxyImages));
			entry.setAuthor(content.getAuthor());
			entry.setEnclosureUrl(content.getEnclosureUrl());
			entry.setEnclosureType(content.getEnclosureType());
		}

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

	@ApiProperty("wether the entry is still markable (old entry statuses are discarded)")
	private boolean markable;
}
