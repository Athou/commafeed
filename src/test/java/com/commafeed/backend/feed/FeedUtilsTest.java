package com.commafeed.backend.feed;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FeedUtilsTest {

	@Test
	public void testNormalization() {
		final String urla1 = "http://example.com/hello?a=1&b=2";
		final String urla2 = "http://www.example.com/hello?a=1&b=2";
		final String urla3 = "http://EXAmPLe.com/HELLo?a=1&b=2";
		final String urla4 = "http://example.com/hello?b=2&a=1";
		final String urla5 = "https://example.com/hello?a=1&b=2";

		final String urlb1 = "http://ftr.fivefilters.org/makefulltextfeed.php?url=http%3A%2F%2Ffeeds.howtogeek.com%2FHowToGeek&max=10&summary=1";
		final String urlb2 = "http://ftr.fivefilters.org/makefulltextfeed.php?url=http://feeds.howtogeek.com/HowToGeek&max=10&summary=1";

		final String urlc1 = "http://feeds.feedburner.com/Frandroid";
		final String urlc2 = "http://feeds2.feedburner.com/frandroid";
		final String urlc3 = "http://feedproxy.google.com/frandroid";
		final String urlc4 = "http://feeds.feedburner.com/Frandroid/";
		final String urlc5 = "http://feeds.feedburner.com/Frandroid?format=rss";

		final String urld1 = "http://fivefilters.org/content-only/makefulltextfeed.php?url=http://feeds.feedburner.com/Frandroid";
		final String urld2 = "http://fivefilters.org/content-only/makefulltextfeed.php?url=http://feeds2.feedburner.com/Frandroid";

		assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla2));
		assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla3));
		assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla4));
		assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla5));

		assertEquals(FeedUtils.normalizeURL(urlb1), FeedUtils.normalizeURL(urlb2));

		assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc2));
		assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc3));
		assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc4));
		assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc5));

		assertNotEquals(FeedUtils.normalizeURL(urld1), FeedUtils.normalizeURL(urld2));
	}

	@Test
	public void testToAbsoluteUrl() {
		final String expected = "http://a.com/blog/entry/1";

		// usual cases
		assertEquals(expected, FeedUtils.toAbsoluteUrl("http://a.com/blog/entry/1", "http://a.com/feed/", "http://a.com/feed/"));
		assertEquals(expected, FeedUtils.toAbsoluteUrl("http://a.com/blog/entry/1", "http://a.com/feed", "http://a.com/feed"));

		// relative links
		assertEquals(expected, FeedUtils.toAbsoluteUrl("../blog/entry/1", "http://a.com/feed/", "http://a.com/feed/"));
		assertEquals(expected, FeedUtils.toAbsoluteUrl("../blog/entry/1", "feed.xml", "http://a.com/feed/feed.xml"));

		// root-relative links
		assertEquals(expected, FeedUtils.toAbsoluteUrl("/blog/entry/1", "/feed", "http://a.com/feed"));

		// real cases
		assertEquals("https://github.com/erusev/parsedown/releases/tag/1.3.0", FeedUtils.toAbsoluteUrl(
				"/erusev/parsedown/releases/tag/1.3.0", "/erusev/parsedown/releases", "https://github.com/erusev/parsedown/tags.atom"));
		assertEquals("http://ergoemacs.org/emacs/elisp_all_about_lines.html",
				FeedUtils.toAbsoluteUrl("elisp_all_about_lines.html", "blog.xml", "http://ergoemacs.org/emacs/blog.xml"));
	}

	@Test
	public void testExtractDeclaredEncoding() {
		assertNull(FeedUtils.extractDeclaredEncoding("<?xml ?>".getBytes()));
		assertNull(FeedUtils.extractDeclaredEncoding("<feed></feed>".getBytes()));
		assertEquals("UTF-8", FeedUtils.extractDeclaredEncoding("<?xml encoding=\"UTF-8\" ?>".getBytes()));
		assertEquals("UTF-8", FeedUtils.extractDeclaredEncoding("<?xml encoding='UTF-8' ?>".getBytes()));
		assertEquals("UTF-8", FeedUtils.extractDeclaredEncoding("<?xml encoding='UTF-8'?>".getBytes()));
	}

	@Test
	public void testReplaceHtmlEntitiesWithNumericEntities() {
		final String source = "<source>T&acute;l&acute;phone &prime;</source>";
		assertEquals("<source>T&#180;l&#180;phone &#8242;</source>", FeedUtils.replaceHtmlEntitiesWithNumericEntities(source));
	}

	@Test
	public void testRemoveTrailingSlash() {
		final String url = "http://localhost/";
		final String result = FeedUtils.removeTrailingSlash(url);
		assertEquals("http://localhost", result);
	}

	@Test
	public void testRemoveTrailingSlash_lastSlashOnly() {
		final String url = "http://localhost//";
		final String result = FeedUtils.removeTrailingSlash(url);
		assertEquals("http://localhost/", result);
	}

}
