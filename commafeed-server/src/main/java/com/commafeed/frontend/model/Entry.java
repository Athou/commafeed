package com.commafeed.frontend.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.Strings;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.commafeed.backend.feed.FeedUtils;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryContent;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.FeedSubscription;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@SuppressWarnings("serial")
@Schema(description = "Entry details")
@Data
@RegisterForReflection
public class Entry implements Serializable {

	@Schema(description = "entry id", required = true)
	private String id;

	@Schema(description = "entry guid", required = true)
	private String guid;

	@Schema(description = "entry title", required = true)
	private String title;

	@Schema(description = "entry content", required = true)
	private String content;

	@Schema(description = "comma-separated list of categories")
	private String categories;

	@Schema(description = "whether entry content and title are rtl", required = true)
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

	@Schema(description = "entry publication date", type = SchemaType.INTEGER, required = true)
	private Instant date;

	@Schema(description = "entry insertion date in the database", type = SchemaType.INTEGER, required = true)
	private Instant insertedDate;

	@Schema(description = "feed id", required = true)
	private String feedId;

	@Schema(description = "feed name", required = true)
	private String feedName;

	@Schema(description = "this entry's feed url", required = true)
	private String feedUrl;

	@Schema(description = "this entry's website url", required = true)
	private String feedLink;

	@Schema(description = "The favicon url to use for this feed", required = true)
	private String iconUrl;

	@Schema(description = "entry url", required = true)
	private String url;

	@Schema(description = "read status", required = true)
	private boolean read;

	@Schema(description = "starred status", required = true)
	private boolean starred;

	@Schema(description = "whether the entry is still markable (old entry statuses are discarded)", required = true)
	private boolean markable;

	@Schema(description = "tags", required = true)
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
			entry.setRtl(content.getDirection() == FeedEntryContent.Direction.RTL);
			entry.setTitle(content.getTitle());
			entry.setContent(proxyImages ? FeedUtils.proxyImages(content.getContent()) : content.getContent());
			entry.setAuthor(content.getAuthor());

			entry.setEnclosureType(content.getEnclosureType());
			entry.setEnclosureUrl(proxyImages && Strings.CS.contains(content.getEnclosureType(), "image")
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

}
