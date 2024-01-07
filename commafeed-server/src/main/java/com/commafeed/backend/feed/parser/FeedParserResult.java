package com.commafeed.backend.feed.parser;

import java.util.Date;
import java.util.List;

public record FeedParserResult(String title, String link, Date lastPublishedDate, Long averageEntryInterval, Date lastEntryDate,
		List<Entry> entries) {
	public record Entry(String guid, String url, Date updated, Content content) {
	}

	public record Content(String title, String content, String author, String categories, Enclosure enclosure, Media media) {
	}

	public record Enclosure(String url, String type) {
	}

	public record Media(String description, String thumbnailUrl, Integer thumbnailWidth, Integer thumbnailHeight) {
	}

}
