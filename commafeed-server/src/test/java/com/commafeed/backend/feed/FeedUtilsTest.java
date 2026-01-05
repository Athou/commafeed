package com.commafeed.backend.feed;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.Entry;
import com.rometools.rome.feed.synd.SyndEntry;

class FeedUtilsTest {

	@Test
	void asRss() {
		Entry entry = new Entry();
		entry.setId("1");
		entry.setGuid("guid-1");
		entry.setTitle("Test Entry");
		entry.setContent("This is a test entry content.");
		entry.setCategories("test,example");
		entry.setRtl(false);
		entry.setAuthor("Author Name");
		entry.setEnclosureUrl("http://example.com/enclosure.mp3");
		entry.setEnclosureType("audio/mpeg");
		entry.setDate(Instant.ofEpochSecond(1));
		entry.setUrl("http://example.com/test-entry");

		SyndEntry syndEntry = FeedUtils.asRss(entry);
		Assertions.assertEquals("guid-1", syndEntry.getUri());
		Assertions.assertEquals("Test Entry", syndEntry.getTitle());
		Assertions.assertEquals("Author Name", syndEntry.getAuthor());
		Assertions.assertEquals(1, syndEntry.getContents().size());
		Assertions.assertEquals("This is a test entry content.", syndEntry.getContents().getFirst().getValue());
		Assertions.assertEquals(1, syndEntry.getEnclosures().size());
		Assertions.assertEquals("http://example.com/enclosure.mp3", syndEntry.getEnclosures().getFirst().getUrl());
		Assertions.assertEquals("audio/mpeg", syndEntry.getEnclosures().getFirst().getType());
		Assertions.assertEquals("http://example.com/test-entry", syndEntry.getLink());
		Assertions.assertEquals(Date.from(Instant.ofEpochSecond(1)), syndEntry.getPublishedDate());
	}
}