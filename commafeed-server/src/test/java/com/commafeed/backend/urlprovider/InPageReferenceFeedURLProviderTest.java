package com.commafeed.backend.urlprovider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InPageReferenceFeedURLProviderTest {

	private final InPageReferenceFeedURLProvider provider = new InPageReferenceFeedURLProvider();

	@Test
	void extractsAtomFeedURL() {
		String url = "http://example.com";
		String html = """
				<html>
					<head>
						<link type="application/atom+xml" href="/feed.atom">
					</head>
					<body>
					</body>
				</html>""";

		String result = provider.get(url, html);

		Assertions.assertEquals("http://example.com/feed.atom", result);
	}

	@Test
	void extractsRSSFeedURL() {
		String url = "http://example.com";
		String html = """
				<html>
					<head>
						<link type="application/rss+xml" href="/feed.rss">
					</head>
					<body>
					</body>
				</html>""";

		String result = provider.get(url, html);

		Assertions.assertEquals("http://example.com/feed.rss", result);
	}

	@Test
	void prefersAtomOverRSS() {
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

		String result = provider.get(url, html);

		Assertions.assertEquals("http://example.com/feed.atom", result);
	}

	@Test
	void returnsNullForNonHtmlContent() {
		String url = "http://example.com";
		String content = """
				<?xml version="1.0"?>
					<feed></feed>
				</xml>""";

		String result = provider.get(url, content);

		Assertions.assertNull(result);
	}

	@Test
	void returnsNullForHtmlWithoutFeedLinks() {
		String url = "http://example.com";
		String html = """
				<html>
					<head>
						<link type="text/css" href="/style.css">
					</head>
					<body>
					</body>
				</html>""";

		String result = provider.get(url, html);

		Assertions.assertNull(result);
	}
}