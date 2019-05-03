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
@ApiModel(description = "Entry details")
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
					? FeedUtils.proxyImage(content.getEnclosureUrl(), publicUrl)
					: content.getEnclosureUrl());
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

	@ApiModelProperty(value = "entry id", required = true)
	private String id;

	@ApiModelProperty(value = "entry guid", required = true)
	private String guid;

	@ApiModelProperty(value = "entry title", required = true)
	private String title;

	@ApiModelProperty(value = "entry content", required = true)
	private String content;

	@ApiModelProperty(value = "comma-separated list of categories")
	private String categories;

	@ApiModelProperty(value = "wether entry content and title are rtl", required = true)
	private boolean rtl;

	@ApiModelProperty(value = "entry author")
	private String author;

	@ApiModelProperty(value = "entry enclosure url, if any")
	private String enclosureUrl;

	@ApiModelProperty(value = "entry enclosure mime type, if any")
	private String enclosureType;

	@ApiModelProperty(value = "entry publication date", dataType = "number", required = true)
	private Date date;

	@ApiModelProperty(value = "entry insertion date in the database", dataType = "number", required = true)
	private Date insertedDate;

	@ApiModelProperty(value = "feed id", required = true)
	private String feedId;

	@ApiModelProperty(value = "feed name", required = true)
	private String feedName;

	@ApiModelProperty(value = "this entry's feed url", required = true)
	private String feedUrl;

	@ApiModelProperty(value = "this entry's website url", required = true)
	private String feedLink;

	@ApiModelProperty(value = "The favicon url to use for this feed", required = true)
	private String iconUrl;

	@ApiModelProperty(value = "entry url", required = true)
	private String url;

	@ApiModelProperty(value = "read status", required = true)
	private boolean read;

	@ApiModelProperty(value = "starred status", required = true)
	private boolean starred;

	@ApiModelProperty(value = "wether the entry is still markable (old entry statuses are discarded)", required = true)
	private boolean markable;

	@ApiModelProperty(value = "tags", required = true)
	private List<String> tags;
}
