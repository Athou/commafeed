package com.commafeed.backend.feeds;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.google.api.client.util.Lists;

public class FeedUtils {

	protected static Logger log = LoggerFactory.getLogger(FeedUtils.class);

	public static String truncate(String string, int length) {
		if (string != null) {
			string = string.substring(0, Math.min(length, string.length()));
		}
		return string;
	}

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

	public static FeedEntry findEntry(Collection<FeedEntry> list,
			FeedEntry entry) {
		FeedEntry found = null;
		for (FeedEntry e : list) {
			if (StringUtils.equals(entry.getGuid(), e.getGuid())
					&& StringUtils.equals(entry.getUrl(), e.getUrl())) {
				found = e;
				break;
			}
		}
		return found;
	}

	public static Feed findFeed(Collection<Feed> list, Feed feed) {
		Feed found = null;
		for (Feed f : list) {
			if (ObjectUtils.equals(feed.getId(), f.getId())) {
				found = f;
				break;
			}
		}
		return found;
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
			disabledHours = Math.min(24 * 7, disabledHours);
			return DateUtils.addHours(now, disabledHours);
		}
		return null;
	}

	/**
	 * When the feed was refreshed successfully
	 */
	public static Date buildDisabledUntil(Date publishedDate,
			List<FeedEntry> entries) {
		Date now = Calendar.getInstance().getTime();

		if (publishedDate == null) {
			// feed with no entries, recheck in 24 hours
			return DateUtils.addHours(now, 24);
		} else if (publishedDate.before(DateUtils.addMonths(now, -1))) {
			// older than a month, recheck in 24 hours
			return DateUtils.addHours(now, 24);
		} else if (publishedDate.before(DateUtils.addDays(now, -14))) {
			// older than two weeks, recheck in 12 hours
			return DateUtils.addHours(now, 12);
		} else if (publishedDate.before(DateUtils.addDays(now, -7))) {
			// older than a week, recheck in 6 hours
			return DateUtils.addHours(now, 6);
		} else if (CollectionUtils.isNotEmpty(entries)) {
			// use average time between entries to decide when to refresh next
			long average = averageTimeBetweenEntries(entries);
			int factor = 2;
			return new Date(Math.min(DateUtils.addHours(now, 6).getTime(),
					now.getTime() + average / factor));
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
		for (FeedEntry entry : entries) {
			timestamps.add(entry.getUpdated().getTime());
		}
		Collections.sort(timestamps);
		Collections.reverse(timestamps);
		return timestamps;
	}

	public static String removeTrailingSlash(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	public static String toAbsoluteUrl(String url, String baseUrl) {
		if (baseUrl == null || url == null || url.startsWith("http")) {
			return url;
		}

		if (url.startsWith("/") == false) {
			url = "/" + url;
		}

		return baseUrl + url;
	}

	public static String getFaviconUrl(String url, String publicUrl) {

		String defaultIcon = removeTrailingSlash(publicUrl)
				+ "/images/default_favicon.gif";
		if (StringUtils.isBlank(url)) {
			return defaultIcon;
		}

		int index = Math.max(url.length(), url.lastIndexOf('?'));

		StringBuilder iconUrl = new StringBuilder(
				"https://getfavicon.appspot.com/");
		try {
			iconUrl.append(URLEncoder.encode(url.substring(0, index), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// never happens
			log.error(e.getMessage(), e);
		}
		iconUrl.append("?defaulticon=none");
		return iconUrl.toString();
	}
}
