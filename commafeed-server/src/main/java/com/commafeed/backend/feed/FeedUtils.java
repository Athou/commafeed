package com.commafeed.backend.feed;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.utils.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;

import com.commafeed.backend.feed.FeedEntryKeyword.Mode;
import com.commafeed.backend.feed.parser.TextDirectionDetector;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.frontend.model.Entry;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods related to feed handling
 * 
 */
@UtilityClass
@Slf4j
public class FeedUtils {

	private static final String ESCAPED_QUESTION_MARK = Pattern.quote("?");

	public static String truncate(String string, int length) {
		if (string != null) {
			string = string.substring(0, Math.min(length, string.length()));
		}
		return string;
	}

	public static boolean isHttp(String url) {
		return url.startsWith("http://");
	}

	public static boolean isHttps(String url) {
		return url.startsWith("https://");
	}

	public static boolean isAbsoluteUrl(String url) {
		return isHttp(url) || isHttps(url);
	}

	/**
	 * Normalize the url. The resulting url is not meant to be fetched but rather used as a mean to identify a feed and avoid duplicates
	 */
	public static String normalizeURL(String url) {
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
			normalized = StringUtils.removeEnd(normalized, "/");
		}

		return normalized;
	}

	public static boolean isRTL(String title, String content) {
		String text = StringUtils.isNotBlank(content) ? content : title;
		if (StringUtils.isBlank(text)) {
			return false;
		}

		String stripped = Jsoup.parse(text).text();
		if (StringUtils.isBlank(stripped)) {
			return false;
		}

		return TextDirectionDetector.detect(stripped) == TextDirectionDetector.Direction.RIGHT_TO_LEFT;
	}

	public static String removeTrailingSlash(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
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
	public static String toAbsoluteUrl(String relativeUrl, String feedLink, String feedUrl) {
		String baseUrl = (feedLink != null && isAbsoluteUrl(feedLink)) ? feedLink : feedUrl;
		if (baseUrl == null) {
			return null;
		}

		return URI.create(baseUrl).resolve(relativeUrl).toString();
	}

	public static String getFaviconUrl(FeedSubscription subscription) {
		return "rest/feed/favicon/" + subscription.getId();
	}

	public static String proxyImages(String content) {
		if (StringUtils.isBlank(content)) {
			return content;
		}

		Document doc = Jsoup.parse(content);
		Elements elements = doc.select("img");
		for (Element element : elements) {
			String href = element.attr("src");
			if (StringUtils.isNotBlank(href)) {
				String proxy = proxyImage(href);
				element.attr("src", proxy);
			}
		}

		return doc.body().html();
	}

	public static String proxyImage(String url) {
		if (StringUtils.isBlank(url)) {
			return url;
		}
		return "rest/server/proxy?u=" + imageProxyEncoder(url);
	}

	public static String rot13(String msg) {
		StringBuilder message = new StringBuilder();

		for (char c : msg.toCharArray()) {
			if (c >= 'a' && c <= 'm') {
				c += 13;
			} else if (c >= 'n' && c <= 'z') {
				c -= 13;
			} else if (c >= 'A' && c <= 'M') {
				c += 13;
			} else if (c >= 'N' && c <= 'Z') {
				c -= 13;
			}
			message.append(c);
		}

		return message.toString();
	}

	public static String imageProxyEncoder(String url) {
		return Base64.encodeBase64String(rot13(url).getBytes());
	}

	public static String imageProxyDecoder(String code) {
		return rot13(new String(Base64.decodeBase64(code)));
	}

	public static void removeUnwantedFromSearch(List<Entry> entries, List<FeedEntryKeyword> keywords) {
		if (keywords.isEmpty()) {
			return;
		}

		entries.removeIf(e -> {
			String title = e.getTitle() == null ? null : Jsoup.parse(e.getTitle()).text();
			String content = e.getContent() == null ? null : Jsoup.parse(e.getContent()).text();
			for (FeedEntryKeyword keyword : keywords) {
				boolean condition = !StringUtils.containsIgnoreCase(content, keyword.getKeyword())
						&& !StringUtils.containsIgnoreCase(title, keyword.getKeyword());
				if (keyword.getMode() == Mode.EXCLUDE) {
					condition = !condition;
				}
				if (condition) {
					return true;
				}
			}
			return false;
		});
	}
}
