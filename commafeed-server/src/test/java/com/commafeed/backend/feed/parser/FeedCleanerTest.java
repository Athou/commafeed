package com.commafeed.backend.feed.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FeedCleanerTest {

	FeedCleaner feedCleaner = new FeedCleaner();

	@Test
	void testReplaceHtmlEntitiesWithNumericEntities() {
		String source = "<source>T&acute;l&acute;phone &prime;</source>";
		Assertions.assertEquals("<source>T&#180;l&#180;phone &#8242;</source>", feedCleaner.replaceHtmlEntitiesWithNumericEntities(source));
	}

}