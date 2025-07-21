package com.commafeed.backend.urlprovider;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InPageReferenceFeedURLProviderTest {

	private final InPageReferenceFeedURLProvider provider = new InPageReferenceFeedURLProvider();

	@Test
	void extractUrls() {
		String url = "http://example.com";
		String html = """
				<html>
					<head>
						<link type="application/atom+xml" href="/feed.atom">
						<link type="application/rss+xml" href="/feed.rss">
					</head>
					<body>
					</body>
				</html>""";

		Assertions.assertIterableEquals(List.of("http://example.com/feed.atom", "http://example.com/feed.rss"), provider.get(url, html));
	}

	@Test
	void returnsEmptyListForNonHtmlContent() {
		String url = "http://example.com";
		String html = """
				<?xml version="1.0"?>
					<feed></feed>
				</xml>""";

		Assertions.assertTrue(provider.get(url, html).isEmpty());
	}

	@Test
	void returnsEmptyListForHtmlWithoutFeedLinks() {
		String url = "http://example.com";
		String html = """
				<html>
					<head>
						<link type="text/css" href="/style.css">
					</head>
					<body>
					</body>
				</html>""";

		Assertions.assertTrue(provider.get(url, html).isEmpty());
	}
}