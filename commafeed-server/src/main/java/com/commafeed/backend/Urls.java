package com.commafeed.backend;

import java.net.URI;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Strings;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class Urls {

	private static final String ESCAPED_QUESTION_MARK = Pattern.quote("?");

	public static boolean isHttp(String url) {
		return url.startsWith("http://");
	}

	public static boolean isHttps(String url) {
		return url.startsWith("https://");
	}

	public static boolean isAbsolute(String url) {
		return isHttp(url) || isHttps(url);
	}

	/**
	 *
	 * @param relativeUrl
	 *            the url of the entry
	 * @param feedLink
	 *            the url of the feed as described in the feed
	 * @param feedUrl
	 *            the url of the feed that we used to fetch the feed
	 * @return an absolute url pointing to the entry
	 */
	public static String toAbsolute(String relativeUrl, String feedLink, String feedUrl) {
		String baseUrl = (feedLink != null && isAbsolute(feedLink)) ? feedLink : feedUrl;
		if (baseUrl == null) {
			return null;
		}

		try {
			return URI.create(baseUrl).resolve(relativeUrl).toString();
		} catch (IllegalArgumentException e) {
			log.debug("Unable to create absolute url from relative url: {} base: {}", relativeUrl, baseUrl, e);
			return null;
		}
	}

	public static String removeTrailingSlash(String url) {
		if (url == null) {
			return null;
		}

		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	/**
	 * Normalize the url. The resulting url is not meant to be fetched but rather used as a mean to identify a feed and avoid duplicates
	 */
	public static String normalize(String url) {
		if (url == null) {
			return null;
		}

		ParsedUrl parsedUrl = ParsedUrl.parseUrl(url);
		Canonicalizer.AGGRESSIVE.canonicalize(parsedUrl);
		String normalized = parsedUrl.toString();
		if (normalized == null) {
			normalized = url;
		}

		// convert to lower case, the url probably won't work in some cases
		// after that but we don't care we just want to compare urls to avoid
		// duplicates
		normalized = normalized.toLowerCase();

		// store all urls as http
		if (normalized.startsWith("https")) {
			normalized = "http" + normalized.substring(5);
		}

		// remove the www. part
		normalized = normalized.replace("//www.", "//");

		// feedproxy redirects to feedburner
		normalized = normalized.replace("feedproxy.google.com", "feeds.feedburner.com");

		// feedburner feeds have a special treatment
		if (normalized.split(ESCAPED_QUESTION_MARK)[0].contains("feedburner.com")) {
			normalized = normalized.replace("feeds2.feedburner.com", "feeds.feedburner.com");
			normalized = normalized.split(ESCAPED_QUESTION_MARK)[0];
			normalized = Strings.CS.removeEnd(normalized, "/");
		}

		return normalized;
	}
}
