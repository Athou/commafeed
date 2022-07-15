package com.commafeed.backend.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FeedUtilsTest {

	@Test
	void testNormalization() {
		String urla1 = "http://example.com/hello?a=1&b=2";
		String urla2 = "http://www.example.com/hello?a=1&b=2";
		String urla3 = "http://EXAmPLe.com/HELLo?a=1&b=2";
		String urla4 = "http://example.com/hello?b=2&a=1";
		String urla5 = "https://example.com/hello?a=1&b=2";

		String urlb1 = "http://ftr.fivefilters.org/makefulltextfeed.php?url=http%3A%2F%2Ffeeds.howtogeek.com%2FHowToGeek&max=10&summary=1";
		String urlb2 = "http://ftr.fivefilters.org/makefulltextfeed.php?url=http://feeds.howtogeek.com/HowToGeek&max=10&summary=1";

		String urlc1 = "http://feeds.feedburner.com/Frandroid";
		String urlc2 = "http://feeds2.feedburner.com/frandroid";
		String urlc3 = "http://feedproxy.google.com/frandroid";
		String urlc4 = "http://feeds.feedburner.com/Frandroid/";
		String urlc5 = "http://feeds.feedburner.com/Frandroid?format=rss";

		String urld1 = "http://fivefilters.org/content-only/makefulltextfeed.php?url=http://feeds.feedburner.com/Frandroid";
		String urld2 = "http://fivefilters.org/content-only/makefulltextfeed.php?url=http://feeds2.feedburner.com/Frandroid";

		Assertions.assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla2));
		Assertions.assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla3));
		Assertions.assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla4));
		Assertions.assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla5));

		Assertions.assertEquals(FeedUtils.normalizeURL(urlb1), FeedUtils.normalizeURL(urlb2));

		Assertions.assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc2));
		Assertions.assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc3));
		Assertions.assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc4));
		Assertions.assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc5));

		Assertions.assertNotEquals(FeedUtils.normalizeURL(urld1), FeedUtils.normalizeURL(urld2));

	}

	@Test
	void testToAbsoluteUrl() {
		String expected = "http://a.com/blog/entry/1";

		// usual cases
		Assertions.assertEquals(expected, FeedUtils.toAbsoluteUrl("http://a.com/blog/entry/1", "http://a.com/feed/", "http://a.com/feed/"));
		Assertions.assertEquals(expected, FeedUtils.toAbsoluteUrl("http://a.com/blog/entry/1", "http://a.com/feed", "http://a.com/feed"));

		// relative links
		Assertions.assertEquals(expected, FeedUtils.toAbsoluteUrl("../blog/entry/1", "http://a.com/feed/", "http://a.com/feed/"));
		Assertions.assertEquals(expected, FeedUtils.toAbsoluteUrl("../blog/entry/1", "feed.xml", "http://a.com/feed/feed.xml"));

		// root-relative links
		Assertions.assertEquals(expected, FeedUtils.toAbsoluteUrl("/blog/entry/1", "/feed", "http://a.com/feed"));

		// real cases
		Assertions.assertEquals("https://github.com/erusev/parsedown/releases/tag/1.3.0", FeedUtils.toAbsoluteUrl(
				"/erusev/parsedown/releases/tag/1.3.0", "/erusev/parsedown/releases", "https://github.com/erusev/parsedown/tags.atom"));
		Assertions.assertEquals("http://ergoemacs.org/emacs/elisp_all_about_lines.html",
				FeedUtils.toAbsoluteUrl("elisp_all_about_lines.html", "blog.xml", "http://ergoemacs.org/emacs/blog.xml"));

	}

	@Test
	void testExtractDeclaredEncoding() {
		Assertions.assertNull(FeedUtils.extractDeclaredEncoding("<?xml ?>".getBytes()));
		Assertions.assertNull(FeedUtils.extractDeclaredEncoding("<feed></feed>".getBytes()));
		Assertions.assertEquals("UTF-8", FeedUtils.extractDeclaredEncoding("<?xml encoding=\"UTF-8\" ?>".getBytes()));
		Assertions.assertEquals("UTF-8", FeedUtils.extractDeclaredEncoding("<?xml encoding='UTF-8' ?>".getBytes()));
		Assertions.assertEquals("UTF-8", FeedUtils.extractDeclaredEncoding("<?xml encoding='UTF-8'?>".getBytes()));
	}

	@Test
	void testReplaceHtmlEntitiesWithNumericEntities() {
		String source = "<source>T&acute;l&acute;phone &prime;</source>";
		Assertions.assertEquals("<source>T&#180;l&#180;phone &#8242;</source>", FeedUtils.replaceHtmlEntitiesWithNumericEntities(source));
	}

	@Test
	void testRemoveTrailingSlash() {
		final String url = "http://localhost/";
		final String result = FeedUtils.removeTrailingSlash(url);
		Assertions.assertEquals("http://localhost", result);
	}

	@Test
	void testRemoveTrailingSlashLastSlashOnly() {
		final String url = "http://localhost//";
		final String result = FeedUtils.removeTrailingSlash(url);
		Assertions.assertEquals("http://localhost/", result);
	}

}
