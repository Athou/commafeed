package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEnclosureImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@SuppressWarnings("serial")
@ApiModel("Entry details")
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
		entry.setTags(status.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()));

		if (content != null) {
			entry.setRtl(FeedUtils.isRTL(feedEntry));
			entry.setTitle(content.getTitle());
			entry.setContent(proxyImages ? FeedUtils.proxyImages(content.getContent(), publicUrl) : content.getContent());
			entry.setAuthor(content.getAuthor());
			entry.setEnclosureType(content.getEnclosureType());
			entry.setEnclosureUrl(proxyImages && StringUtils.contains(content.getEnclosureType(), "image")
					? FeedUtils.proxyImage(content.getEnclosureUrl(), publicUrl) : content.getEnclosureUrl());
			entry.setCategories(content.getCategories());
		}

		return entry;
	}

	public SyndEntry asRss() {
		SyndEntry entry = new SyndEntryImpl();

		entry.setUri(getGuid());
		entry.setTitle(getTitle());
		entry.setAuthor(getAuthor());

		SyndContentImpl content = new SyndContentImpl();
		content.setValue(getContent());
		entry.setContents(Arrays.<SyndContent> asList(content));

		if (getEnclosureUrl() != null) {
			SyndEnclosureImpl enclosure = new SyndEnclosureImpl();
			enclosure.setType(getEnclosureType());
			enclosure.setUrl(getEnclosureUrl());
			entry.setEnclosures(Arrays.<SyndEnclosure> asList(enclosure));
		}

		entry.setLink(getUrl());
		entry.setPublishedDate(getDate());
		return entry;
	}

	@ApiModelProperty("entry id")
	private String id;

	@ApiModelProperty("entry guid")
	private String guid;

	@ApiModelProperty("entry title")
	private String title;

	@ApiModelProperty("entry content")
	private String content;

	@ApiModelProperty("comma-separated list of categories")
	private String categories;

	@ApiModelProperty("wether entry content and title are rtl")
	private boolean rtl;

	@ApiModelProperty("entry author")
	private String author;

	@ApiModelProperty("entry enclosure url, if any")
	private String enclosureUrl;

	@ApiModelProperty("entry enclosure mime type, if any")
	private String enclosureType;

	@ApiModelProperty("entry publication date")
	private Date date;

	@ApiModelProperty("entry insertion date in the database")
	private Date insertedDate;

	@ApiModelProperty("feed id")
	private String feedId;

	@ApiModelProperty("feed name")
	private String feedName;

	@ApiModelProperty("this entry's feed url")
	private String feedUrl;

	@ApiModelProperty("this entry's website url")
	private String feedLink;

	@ApiModelProperty(value = "The favicon url to use for this feed")
	private String iconUrl;

	@ApiModelProperty("entry url")
	private String url;

	@ApiModelProperty("read sttaus")
	private boolean read;

	@ApiModelProperty("starred status")
	private boolean starred;

	@ApiModelProperty("wether the entry is still markable (old entry statuses are discarded)")
	private boolean markable;

	@ApiModelProperty("tags")
	private List<String> tags;
}
