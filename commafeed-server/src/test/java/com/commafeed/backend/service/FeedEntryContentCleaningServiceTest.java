package com.commafeed.backend.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FeedEntryContentCleaningServiceTest {

	private final FeedEntryContentCleaningService feedEntryContentCleaningService = new FeedEntryContentCleaningService();

	@Test
	void testClean() {
		String content = """
				<p>
				Some text
				<img width="965" height="320" src="https://localhost/an-image.png" class="attachment-post-thumbnail size-post-thumbnail wp-post-image" alt="alt-desc" decoding="async" sizes="(max-width: 965px) 100vw, 965px" style="width: 100%; opacity: 0">
				<iframe src="url" style="width: 100%; opacity: 0"></iframe>
				<forbidden-element>aaa</forbidden-element>
				""";
		String result = feedEntryContentCleaningService.clean(content, "baseUri", false);

		Assertions.assertLinesMatch("""
				<p>
				Some text
				<img width="965" height="320" src="https://localhost/an-image.png" alt="alt-desc" style="width:100%;">
				<iframe src="url" style="width:100%;"></iframe>
				aaa
				</p>
				""".lines(), result.lines());
	}

}