package com.commafeed.backend.feed;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;

import com.commafeed.backend.feed.FeedEntryKeyword.Mode;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.frontend.model.Entry;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.i18n.shared.BidiUtils;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods related to feed handling
 * 
 */
@Slf4j
public class FeedUtils {

	private static final String ESCAPED_QUESTION_MARK = Pattern.quote("?");

	public static String truncate(String string, int length) {
		if (string != null) {
			string = string.substring(0, Math.min(length, string.length()));
		}
		return string;
	}

	/**
	 * Detect feed encoding by using the declared encoding in the xml processing instruction and by detecting the characters used in the
	 * feed
	 * 
	 */
	public static Charset guessEncoding(byte[] bytes) {
		String extracted = extractDeclaredEncoding(bytes);
		if (StringUtils.startsWithIgnoreCase(extracted, "iso-8859-")) {
			if (!StringUtils.endsWith(extracted, "1")) {
				return Charset.forName(extracted);
			}
		} else if (StringUtils.startsWithIgnoreCase(extracted, "windows-")) {
			return Charset.forName(extracted);
		}
		return detectEncoding(bytes);
	}

	/**
	 * Detect encoding by analyzing characters in the array
	 */
	public static Charset detectEncoding(byte[] bytes) {
		String encoding = "UTF-8";

		CharsetDetector detector = new CharsetDetector();
		detector.setText(bytes);
		CharsetMatch match = detector.detect();
		if (match != null) {
			encoding = match.getName();
		}
		if (encoding.equalsIgnoreCase("ISO-8859-1")) {
			encoding = "windows-1252";
		}
		return Charset.forName(encoding);
	}

	public static String replaceHtmlEntitiesWithNumericEntities(String source) {
		// Create a buffer sufficiently large that re-allocations are minimized.
		StringBuilder sb = new StringBuilder(source.length() << 1);

		TrieBuilder builder = Trie.builder();
		builder.ignoreOverlaps();

		for (String key : HtmlEntities.HTML_ENTITIES) {
			builder.addKeyword(key);
		}

		Trie trie = builder.build();
		Collection<Emit> emits = trie.parseText(source);

		int prevIndex = 0;
		for (Emit emit : emits) {
			int matchIndex = emit.getStart();

			sb.append(source, prevIndex, matchIndex);
			sb.append(HtmlEntities.HTML_TO_NUMERIC_MAP.get(emit.getKeyword()));
			prevIndex = emit.getEnd() + 1;
		}

		// Add the remainder of the string (contains no more matches).
		sb.append(source.substring(prevIndex));

		return sb.toString();
	}

	public static boolean isHttp(String url) {
		return url.startsWith("http://");
	}

	public static boolean isHttps(String url) {
		return url.startsWith("https://");
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

	/**
	 * Extract the declared encoding from the xml
	 */
	public static String extractDeclaredEncoding(byte[] bytes) {
		int index = ArrayUtils.indexOf(bytes, (byte) '>');
		if (index == -1) {
			return null;
		}

		String pi = new String(ArrayUtils.subarray(bytes, 0, index + 1)).replace('\'', '"');
		index = StringUtils.indexOf(pi, "encoding=\"");
		if (index == -1) {
			return null;
		}
		String encoding = pi.substring(index + 10);
		encoding = encoding.substring(0, encoding.indexOf('"'));
		return encoding;
	}

	public static boolean isRTL(FeedEntry entry) {
		String text = entry.getContent().getContent();

		if (StringUtils.isBlank(text)) {
			text = entry.getContent().getTitle();
		}

		if (StringUtils.isBlank(text)) {
			return false;
		}

		text = Jsoup.parse(text).text();
		if (StringUtils.isBlank(text)) {
			return false;
		}

		Direction direction = BidiUtils.get().estimateDirection(text);
		return direction == Direction.RTL;
	}

	public static String trimInvalidXmlCharacters(String xml) {
		if (StringUtils.isBlank(xml)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();

		boolean firstTagFound = false;
		for (int i = 0; i < xml.length(); i++) {
			char c = xml.charAt(i);

			if (!firstTagFound) {
				if (c == '<') {
					firstTagFound = true;
				} else {
					continue;
				}
			}

			if (c >= 32 || c == 9 || c == 10 || c == 13) {
				if (!Character.isHighSurrogate(c) && !Character.isLowSurrogate(c)) {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	public static Long averageTimeBetweenEntries(List<FeedEntry> entries) {
		if (entries.isEmpty() || entries.size() == 1) {
			return null;
		}

		List<Long> timestamps = getSortedTimestamps(entries);

		SummaryStatistics stats = new SummaryStatistics();
		for (int i = 0; i < timestamps.size() - 1; i++) {
			long diff = Math.abs(timestamps.get(i) - timestamps.get(i + 1));
			stats.addValue(diff);
		}
		return (long) stats.getMean();
	}

	public static List<Long> getSortedTimestamps(List<FeedEntry> entries) {
		return entries.stream().map(t -> t.getUpdated().getTime()).sorted(Collections.reverseOrder()).collect(Collectors.toList());
	}

	public static String removeTrailingSlash(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	/**
	 * 
	 * @param url
	 *            the url of the entry
	 * @param feedLink
	 *            the url of the feed as described in the feed
	 * @param feedUrl
	 *            the url of the feed that we used to fetch the feed
	 * @return an absolute url pointing to the entry
	 */
	public static String toAbsoluteUrl(String url, String feedLink, String feedUrl) {
		url = StringUtils.trimToNull(StringUtils.normalizeSpace(url));
		if (url == null || url.startsWith("http")) {
			return url;
		}

		String baseUrl = (feedLink == null || isRelative(feedLink)) ? feedUrl : feedLink;

		if (baseUrl == null) {
			return url;
		}

		String result = null;
		try {
			result = new URL(new URL(baseUrl), url).toString();
		} catch (MalformedURLException e) {
			log.debug("could not parse url : " + e.getMessage(), e);
			result = url;
		}

		return result;
	}

	public static boolean isRelative(final String url) {
		// the regex means "start with 'scheme://'"
		return url.startsWith("/") || url.startsWith("#") || !url.matches("^\\w+\\:\\/\\/.*");
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
			if (href != null) {
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
		Iterator<Entry> it = entries.iterator();
		while (it.hasNext()) {
			Entry entry = it.next();
			boolean keep = true;
			for (FeedEntryKeyword keyword : keywords) {
				String title = entry.getTitle() == null ? null : Jsoup.parse(entry.getTitle()).text();
				String content = entry.getContent() == null ? null : Jsoup.parse(entry.getContent()).text();
				boolean condition = !StringUtils.containsIgnoreCase(content, keyword.getKeyword())
						&& !StringUtils.containsIgnoreCase(title, keyword.getKeyword());
				if (keyword.getMode() == Mode.EXCLUDE) {
					condition = !condition;
				}
				if (condition) {
					keep = false;
					break;
				}
			}
			if (!keep) {
				it.remove();
			}
		}
	}
}
