package com.commafeed.backend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UrlsTest {

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

		Assertions.assertEquals(Urls.normalize(urla1), Urls.normalize(urla2));
		Assertions.assertEquals(Urls.normalize(urla1), Urls.normalize(urla3));
		Assertions.assertEquals(Urls.normalize(urla1), Urls.normalize(urla4));
		Assertions.assertEquals(Urls.normalize(urla1), Urls.normalize(urla5));

		Assertions.assertEquals(Urls.normalize(urlb1), Urls.normalize(urlb2));

		Assertions.assertEquals(Urls.normalize(urlc1), Urls.normalize(urlc2));
		Assertions.assertEquals(Urls.normalize(urlc1), Urls.normalize(urlc3));
		Assertions.assertEquals(Urls.normalize(urlc1), Urls.normalize(urlc4));
		Assertions.assertEquals(Urls.normalize(urlc1), Urls.normalize(urlc5));

		Assertions.assertNotEquals(Urls.normalize(urld1), Urls.normalize(urld2));

	}

	@Test
	void testToAbsoluteUrl() {
		String expected = "http://a.com/blog/entry/1";

		// usual cases
		Assertions.assertEquals(expected, Urls.toAbsolute("http://a.com/blog/entry/1", "http://a.com/feed/", "http://a.com/feed/"));
		Assertions.assertEquals(expected, Urls.toAbsolute("http://a.com/blog/entry/1", "http://a.com/feed", "http://a.com/feed"));

		// relative links
		Assertions.assertEquals(expected, Urls.toAbsolute("../blog/entry/1", "http://a.com/feed/", "http://a.com/feed/"));
		Assertions.assertEquals(expected, Urls.toAbsolute("../blog/entry/1", "feed.xml", "http://a.com/feed/feed.xml"));

		// root-relative links
		Assertions.assertEquals(expected, Urls.toAbsolute("/blog/entry/1", "/feed", "http://a.com/feed"));

		// real cases
		Assertions.assertEquals("https://github.com/erusev/parsedown/releases/tag/1.3.0", Urls.toAbsolute(
				"/erusev/parsedown/releases/tag/1.3.0", "/erusev/parsedown/releases", "https://github.com/erusev/parsedown/tags.atom"));
		Assertions.assertEquals("http://ergoemacs.org/emacs/elisp_all_about_lines.html",
				Urls.toAbsolute("elisp_all_about_lines.html", "blog.xml", "http://ergoemacs.org/emacs/blog.xml"));

		// invalid relative urls
		Assertions.assertEquals("title:10001280",
				Urls.toAbsolute("title:10001280", "https://www.berliner-zeitung.de", "https://www.berliner-zeitung.de/feed.xml"));

	}

	@Test
	void testRemoveTrailingSlash() {
		final String url = "http://localhost/";
		final String result = Urls.removeTrailingSlash(url);
		Assertions.assertEquals("http://localhost", result);
	}

	@Test
	void testRemoveTrailingSlashLastSlashOnly() {
		final String url = "http://localhost//";
		final String result = Urls.removeTrailingSlash(url);
		Assertions.assertEquals("http://localhost/", result);
	}

}