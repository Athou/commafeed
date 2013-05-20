package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;
import org.mozilla.universalchardet.UniversalDetector;

import com.commafeed.backend.model.FeedEntry;
import com.google.api.client.util.Lists;

public class FeedUtils {

	public static String guessEncoding(byte[] bytes) {
		String DEFAULT_ENCODING = "UTF-8";
		UniversalDetector detector = new UniversalDetector(null);
		detector.handleData(bytes, 0, bytes.length);
		detector.dataEnd();
		String encoding = detector.getDetectedCharset();
		detector.reset();
		if (encoding == null) {
			encoding = DEFAULT_ENCODING;
		} else if (encoding.equalsIgnoreCase("ISO-8859-1")
				|| encoding.equalsIgnoreCase("ISO-8859-2")) {
			encoding = "windows-1252";
		}
		return encoding;
	}

	public static String handleContent(String content, String baseUri) {
		if (StringUtils.isNotBlank(content)) {
			baseUri = StringUtils.trimToEmpty(baseUri);
			Whitelist whitelist = new Whitelist();
			whitelist.addTags("a", "b", "blockquote", "br", "caption", "cite",
					"code", "col", "colgroup", "dd", "div", "dl", "dt", "em",
					"h1", "h2", "h3", "h4", "h5", "h6", "i", "iframe", "img",
					"li", "ol", "p", "pre", "q", "small", "strike", "strong",
					"sub", "sup", "table", "tbody", "td", "tfoot", "th",
					"thead", "tr", "u", "ul");

			whitelist.addAttributes("a", "href", "title");
			whitelist.addAttributes("blockquote", "cite");
			whitelist.addAttributes("col", "span", "width");
			whitelist.addAttributes("colgroup", "span", "width");
			whitelist.addAttributes("iframe", "src", "height", "width",
					"allowfullscreen", "frameborder");
			whitelist.addAttributes("img", "alt", "height", "src", "title",
					"width");
			whitelist.addAttributes("ol", "start", "type");
			whitelist.addAttributes("q", "cite");
			whitelist.addAttributes("table", "border", "bordercolor",
					"summary", "width");
			whitelist.addAttributes("td", "border", "bordercolor", "abbr",
					"axis", "colspan", "rowspan", "width");
			whitelist.addAttributes("th", "border", "bordercolor", "abbr",
					"axis", "colspan", "rowspan", "scope", "width");
			whitelist.addAttributes("ul", "type");

			whitelist.addProtocols("a", "href", "ftp", "http", "https",
					"mailto");
			whitelist.addProtocols("blockquote", "cite", "http", "https");
			whitelist.addProtocols("img", "src", "http", "https");
			whitelist.addProtocols("q", "cite", "http", "https");

			whitelist.addEnforcedAttribute("a", "target", "_blank");

			content = Jsoup.clean(content, baseUri, whitelist,
					new OutputSettings().escapeMode(EscapeMode.base)
							.prettyPrint(false));
		}
		return content;
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
				if (!Character.isHighSurrogate(c)
						&& !Character.isLowSurrogate(c)) {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * When there was an error fetching the feed
	 * 
	 */
	public static Date buildDisabledUntil(int errorCount) {
		Date now = Calendar.getInstance().getTime();
		int retriesBeforeDisable = 3;

		if (errorCount >= retriesBeforeDisable) {
			int disabledHours = errorCount - retriesBeforeDisable + 1;
			disabledHours = Math.min(24, disabledHours);
			return DateUtils.addHours(now, disabledHours);
		}
		return null;
	}

	/**
	 * When the feed was refreshed successfully
	 */
	public static Date buildDisabledUntil(FetchedFeed feed) {
		Date now = Calendar.getInstance().getTime();
		Date publishedDate = feed.getPublishedDate();

		if (publishedDate == null) {
			// feed with no entries, recheck in 24 hours
			return DateUtils.addHours(now, 24);
		} else if (publishedDate.before(DateUtils.addMonths(now, -1))) {
			// older tahn a month, recheck in 24 hours
			return DateUtils.addHours(now, 24);
		} else if (publishedDate.before(DateUtils.addDays(now, -14))) {
			// older than two weeks, recheck in 12 hours
			return DateUtils.addHours(now, 12);
		} else if (publishedDate.before(DateUtils.addDays(now, -7))) {
			// older than a week, recheck in 6 hours
			return DateUtils.addHours(now, 6);
		} else if (CollectionUtils.isNotEmpty(feed.getEntries())) {
			// use average time between entries to decide when to refresh next
			long average = averageTimeBetweenEntries(feed.getEntries());
			return new Date(Math.min(DateUtils.addHours(now, 6).getTime(),
					now.getTime() + average / 3));
		} else {
			// unknown case, recheck in 24 hours
			return DateUtils.addHours(now, 24);
		}
	}

	public static long averageTimeBetweenEntries(List<FeedEntry> entries) {
		List<Long> timestamps = getSortedTimestamps(entries);

		SummaryStatistics stats = new SummaryStatistics();
		for (int i = 0; i < timestamps.size() - 1; i++) {
			long diff = Math.abs(timestamps.get(i) - timestamps.get(i + 1));
			stats.addValue(diff);
		}
		return (long) stats.getMean();
	}

	public static List<Long> getSortedTimestamps(List<FeedEntry> entries) {
		List<Long> timestamps = Lists.newArrayList();
		int i = 0;
		for (FeedEntry entry : entries) {
			timestamps.add(entry.getUpdated().getTime());
			i++;
			if (i >= 10)
				break;
		}
		Collections.sort(timestamps);
		Collections.reverse(timestamps);
		return timestamps;
	}
}
