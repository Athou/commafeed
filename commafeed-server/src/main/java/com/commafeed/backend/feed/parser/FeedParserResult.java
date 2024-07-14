package com.commafeed.backend.feed.parser;

import java.time.Instant;
import java.util.List;

public record FeedParserResult(String title, String link, Instant lastPublishedDate, Long averageEntryInterval, Instant lastEntryDate,
		List<Entry> entries) {
	public record Entry(String guid, String url, Instant published, Content content) {
	}

	public record Content(String title, String content, String author, String categories, Enclosure enclosure, Media media) {
	}

	public record Enclosure(String url, String type) {
	}

	public record Media(String description, String thumbnailUrl, Integer thumbnailWidth, Integer thumbnailHeight) {
	}

}
