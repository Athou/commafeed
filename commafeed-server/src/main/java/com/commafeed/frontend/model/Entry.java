package com.commafeed.frontend.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.FeedSubscription;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEnclosureImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Entry details")
@Data
@RegisterForReflection
public class Entry implements Serializable {

	@Schema(description = "entry id", requiredMode = RequiredMode.REQUIRED)
	private String id;

	@Schema(description = "entry guid", requiredMode = RequiredMode.REQUIRED)
	private String guid;

	@Schema(description = "entry title", requiredMode = RequiredMode.REQUIRED)
	private String title;

	@Schema(description = "entry content", requiredMode = RequiredMode.REQUIRED)
	private String content;

	@Schema(description = "comma-separated list of categories")
	private String categories;

	@Schema(description = "whether entry content and title are rtl", requiredMode = RequiredMode.REQUIRED)
	private boolean rtl;

	@Schema(description = "entry author")
	private String author;

	@Schema(description = "entry enclosure url, if any")
	private String enclosureUrl;

	@Schema(description = "entry enclosure mime type, if any")
	private String enclosureType;

	@Schema(description = "entry media description, if any")
	private String mediaDescription;

	@Schema(description = "entry media thumbnail url, if any")
	private String mediaThumbnailUrl;

	@Schema(description = "entry media thumbnail width, if any")
	private Integer mediaThumbnailWidth;

	@Schema(description = "entry media thumbnail height, if any")
	private Integer mediaThumbnailHeight;

	@Schema(description = "entry publication date", type = "number", requiredMode = RequiredMode.REQUIRED)
	private Instant date;

	@Schema(description = "entry insertion date in the database", type = "number", requiredMode = RequiredMode.REQUIRED)
	private Instant insertedDate;

	@Schema(description = "feed id", requiredMode = RequiredMode.REQUIRED)
	private String feedId;

	@Schema(description = "feed name", requiredMode = RequiredMode.REQUIRED)
	private String feedName;

	@Schema(description = "this entry's feed url", requiredMode = RequiredMode.REQUIRED)
	private String feedUrl;

	@Schema(description = "this entry's website url", requiredMode = RequiredMode.REQUIRED)
	private String feedLink;

	@Schema(description = "The favicon url to use for this feed", requiredMode = RequiredMode.REQUIRED)
	private String iconUrl;

	@Schema(description = "entry url", requiredMode = RequiredMode.REQUIRED)
	private String url;

	@Schema(description = "read status", requiredMode = RequiredMode.REQUIRED)
	private boolean read;

	@Schema(description = "starred status", requiredMode = RequiredMode.REQUIRED)
	private boolean starred;

	@Schema(description = "whether the entry is still markable (old entry statuses are discarded)", requiredMode = RequiredMode.REQUIRED)
	private boolean markable;

	@Schema(description = "tags", requiredMode = RequiredMode.REQUIRED)
	private List<String> tags;

	public static Entry build(FeedEntryStatus status, boolean proxyImages) {
		Entry entry = new Entry();

		FeedEntry feedEntry = status.getEntry();
		FeedSubscription sub = status.getSubscription();
		FeedEntryContent content = feedEntry.getContent();

		entry.setId(String.valueOf(feedEntry.getId()));
		entry.setGuid(feedEntry.getGuid());
		entry.setRead(status.isRead());
		entry.setStarred(status.isStarred());
		entry.setMarkable(status.isMarkable());
		entry.setDate(feedEntry.getPublished());
		entry.setInsertedDate(feedEntry.getInserted());
		entry.setUrl(feedEntry.getUrl());
		entry.setFeedName(sub.getTitle());
		entry.setFeedId(String.valueOf(sub.getId()));
		entry.setFeedUrl(sub.getFeed().getUrl());
		entry.setFeedLink(sub.getFeed().getLink());
		entry.setIconUrl(FeedUtils.getFaviconUrl(sub));
		entry.setTags(status.getTags().stream().map(FeedEntryTag::getName).toList());

		if (content != null) {
			entry.setRtl(content.isRTL());
			entry.setTitle(content.getTitle());
			entry.setContent(proxyImages ? FeedUtils.proxyImages(content.getContent()) : content.getContent());
			entry.setAuthor(content.getAuthor());

			entry.setEnclosureType(content.getEnclosureType());
			entry.setEnclosureUrl(proxyImages && StringUtils.contains(content.getEnclosureType(), "image")
					? FeedUtils.proxyImage(content.getEnclosureUrl())
					: content.getEnclosureUrl());

			entry.setMediaDescription(content.getMediaDescription());
			entry.setMediaThumbnailUrl(proxyImages ? FeedUtils.proxyImage(content.getMediaThumbnailUrl()) : content.getMediaThumbnailUrl());
			entry.setMediaThumbnailWidth(content.getMediaThumbnailWidth());
			entry.setMediaThumbnailHeight(content.getMediaThumbnailHeight());

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
		entry.setContents(Collections.singletonList(content));

		if (getEnclosureUrl() != null) {
			SyndEnclosureImpl enclosure = new SyndEnclosureImpl();
			enclosure.setType(getEnclosureType());
			enclosure.setUrl(getEnclosureUrl());
			entry.setEnclosures(Collections.singletonList(enclosure));
		}

		entry.setLink(getUrl());
		entry.setPublishedDate(getDate() == null ? null : Date.from(getDate()));
		return entry;
	}
}
