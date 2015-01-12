package com.commafeed.backend.feed;

import org.junit.Assert;
import org.junit.Test;

public class FeedUtilsTest {

	@Test
	public void testNormalization() {
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

		Assert.assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla2));
		Assert.assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla3));
		Assert.assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla4));
		Assert.assertEquals(FeedUtils.normalizeURL(urla1), FeedUtils.normalizeURL(urla5));

		Assert.assertEquals(FeedUtils.normalizeURL(urlb1), FeedUtils.normalizeURL(urlb2));

		Assert.assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc2));
		Assert.assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc3));
		Assert.assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc4));
		Assert.assertEquals(FeedUtils.normalizeURL(urlc1), FeedUtils.normalizeURL(urlc5));

		Assert.assertNotEquals(FeedUtils.normalizeURL(urld1), FeedUtils.normalizeURL(urld2));

	}

	@Test
	public void testToAbsoluteUrl() {
		String expected = "http://a.com/blog/entry/1";

		// usual cases
		Assert.assertEquals(expected, FeedUtils.toAbsoluteUrl("http://a.com/blog/entry/1", "http://a.com/feed/", "http://a.com/feed/"));
		Assert.assertEquals(expected, FeedUtils.toAbsoluteUrl("http://a.com/blog/entry/1", "http://a.com/feed", "http://a.com/feed"));

		// relative links
		Assert.assertEquals(expected, FeedUtils.toAbsoluteUrl("../blog/entry/1", "http://a.com/feed/", "http://a.com/feed/"));
		Assert.assertEquals(expected, FeedUtils.toAbsoluteUrl("../blog/entry/1", "feed.xml", "http://a.com/feed/feed.xml"));

		// root-relative links
		Assert.assertEquals(expected, FeedUtils.toAbsoluteUrl("/blog/entry/1", "/feed", "http://a.com/feed"));

		// real cases
		Assert.assertEquals("https://github.com/erusev/parsedown/releases/tag/1.3.0", FeedUtils.toAbsoluteUrl(
				"/erusev/parsedown/releases/tag/1.3.0", "/erusev/parsedown/releases", "https://github.com/erusev/parsedown/tags.atom"));
		Assert.assertEquals("http://ergoemacs.org/emacs/elisp_all_about_lines.html",
				FeedUtils.toAbsoluteUrl("elisp_all_about_lines.html", "blog.xml", "http://ergoemacs.org/emacs/blog.xml"));

	}

	@Test
	public void testExtractDeclaredEncoding() {
		Assert.assertNull(FeedUtils.extractDeclaredEncoding("<?xml ?>".getBytes()));
		Assert.assertNull(FeedUtils.extractDeclaredEncoding("<feed></feed>".getBytes()));
		Assert.assertEquals("UTF-8", FeedUtils.extractDeclaredEncoding("<?xml encoding=\"UTF-8\" ?>".getBytes()));
		Assert.assertEquals("UTF-8", FeedUtils.extractDeclaredEncoding("<?xml encoding='UTF-8' ?>".getBytes()));
		Assert.assertEquals("UTF-8", FeedUtils.extractDeclaredEncoding("<?xml encoding='UTF-8'?>".getBytes()));
	}

	@Test
	public void testReplaceHtmlEntitiesWithNumericEntities() {
		String source = "<source>T&acute;l&acute;phone &prime;</source>";
		Assert.assertEquals("<source>T&#180;l&#180;phone &#8242;</source>", FeedUtils.replaceHtmlEntitiesWithNumericEntities(source));
	}
}
