package com.commafeed.backend.feed;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleDeclaration;

import com.commafeed.backend.feed.FeedEntryKeyword.Mode;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.frontend.model.Entry;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.steadystate.css.parser.CSSOMParser;

import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods related to feed handling
 * 
 */
@Slf4j
public class FeedUtils {

	private static final String ESCAPED_QUESTION_MARK = Pattern.quote("?");

	private static final List<String> ALLOWED_IFRAME_CSS_RULES = Arrays.asList("height", "width", "border");
	private static final List<String> ALLOWED_IMG_CSS_RULES = Arrays.asList("display", "width", "height");
	private static final char[] FORBIDDEN_CSS_RULE_CHARACTERS = new char[] { '(', ')' };

	private static final Whitelist WHITELIST = buildWhiteList();

	public static String truncate(String string, int length) {
		if (string != null) {
			string = string.substring(0, Math.min(length, string.length()));
		}
		return string;
	}

	private static synchronized Whitelist buildWhiteList() {
		Whitelist whitelist = new Whitelist();
		whitelist.addTags("a", "b", "blockquote", "br", "caption", "cite", "code", "col", "colgroup", "dd", "div", "dl", "dt", "em", "h1",
				"h2", "h3", "h4", "h5", "h6", "i", "iframe", "img", "li", "ol", "p", "pre", "q", "small", "strike", "strong", "sub", "sup",
				"table", "tbody", "td", "tfoot", "th", "thead", "tr", "u", "ul");

		whitelist.addAttributes("div", "dir");
		whitelist.addAttributes("pre", "dir");
		whitelist.addAttributes("code", "dir");
		whitelist.addAttributes("table", "dir");
		whitelist.addAttributes("p", "dir");
		whitelist.addAttributes("a", "href", "title");
		whitelist.addAttributes("blockquote", "cite");
		whitelist.addAttributes("col", "span", "width");
		whitelist.addAttributes("colgroup", "span", "width");
		whitelist.addAttributes("iframe", "src", "height", "width", "allowfullscreen", "frameborder", "style");
		whitelist.addAttributes("img", "align", "alt", "height", "src", "title", "width", "style");
		whitelist.addAttributes("ol", "start", "type");
		whitelist.addAttributes("q", "cite");
		whitelist.addAttributes("table", "border", "bordercolor", "summary", "width");
		whitelist.addAttributes("td", "border", "bordercolor", "abbr", "axis", "colspan", "rowspan", "width");
		whitelist.addAttributes("th", "border", "bordercolor", "abbr", "axis", "colspan", "rowspan", "scope", "width");
		whitelist.addAttributes("ul", "type");

		whitelist.addProtocols("a", "href", "ftp", "http", "https", "magnet", "mailto");
		whitelist.addProtocols("blockquote", "cite", "http", "https");
		whitelist.addProtocols("img", "src", "http", "https");
		whitelist.addProtocols("q", "cite", "http", "https");

		whitelist.addEnforcedAttribute("a", "target", "_blank");
		whitelist.addEnforcedAttribute("a", "rel", "noreferrer");
		return whitelist;
	}

	/**
	 * Detect feed encoding by using the declared encoding in the xml processing instruction and by detecting the characters used in the
	 * feed
	 * 
	 */
	public static Charset guessEncoding(byte[] bytes) {
		String extracted = extractDeclaredEncoding(bytes);
		if (StringUtils.startsWithIgnoreCase(extracted, "iso-8859-")) {
			if (StringUtils.endsWith(extracted, "1") == false) {
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
		return StringUtils.replaceEach(source, HtmlEntities.HTML_ENTITIES, HtmlEntities.NUMERIC_ENTITIES);
	}

	/**
	 * Normalize the url. The resulting url is not meant to be fetched but rather used as a mean to identify a feed and avoid duplicates
	 */
	public static String normalizeURL(String url) {
		if (url == null) {
			return null;
		}
		String normalized = URLCanonicalizer.getCanonicalURL(url);
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
		String encoding = pi.substring(index + 10, pi.length());
		encoding = encoding.substring(0, encoding.indexOf('"'));
		return encoding;
	}

	public static String handleContent(String content, String baseUri, boolean keepTextOnly) {
		if (StringUtils.isNotBlank(content)) {
			baseUri = StringUtils.trimToEmpty(baseUri);

			Document dirty = Jsoup.parseBodyFragment(content, baseUri);
			Cleaner cleaner = new Cleaner(WHITELIST);
			Document clean = cleaner.clean(dirty);

			for (Element e : clean.select("iframe[style]")) {
				String style = e.attr("style");
				String escaped = escapeIFrameCss(style);
				e.attr("style", escaped);
			}

			for (Element e : clean.select("img[style]")) {
				String style = e.attr("style");
				String escaped = escapeImgCss(style);
				e.attr("style", escaped);
			}

			clean.outputSettings(new OutputSettings().escapeMode(EscapeMode.base).prettyPrint(false));
			Element body = clean.body();
			if (keepTextOnly) {
				content = body.text();
			} else {
				content = body.html();
			}
		}
		return content;
	}

	public static String escapeIFrameCss(String orig) {
		String rule = "";
		CSSOMParser parser = new CSSOMParser();
		try {
			List<String> rules = new ArrayList<>();
			CSSStyleDeclaration decl = parser.parseStyleDeclaration(new InputSource(new StringReader(orig)));

			for (int i = 0; i < decl.getLength(); i++) {
				String property = decl.item(i);
				String value = decl.getPropertyValue(property);
				if (StringUtils.isBlank(property) || StringUtils.isBlank(value)) {
					continue;
				}

				if (ALLOWED_IFRAME_CSS_RULES.contains(property) && StringUtils.containsNone(value, FORBIDDEN_CSS_RULE_CHARACTERS)) {
					rules.add(property + ":" + decl.getPropertyValue(property) + ";");
				}
			}
			rule = StringUtils.join(rules, "");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return rule;
	}

	public static String escapeImgCss(String orig) {
		String rule = "";
		CSSOMParser parser = new CSSOMParser();
		try {
			List<String> rules = new ArrayList<>();
			CSSStyleDeclaration decl = parser.parseStyleDeclaration(new InputSource(new StringReader(orig)));

			for (int i = 0; i < decl.getLength(); i++) {
				String property = decl.item(i);
				String value = decl.getPropertyValue(property);
				if (StringUtils.isBlank(property) || StringUtils.isBlank(value)) {
					continue;
				}

				if (ALLOWED_IMG_CSS_RULES.contains(property) && StringUtils.containsNone(value, FORBIDDEN_CSS_RULE_CHARACTERS)) {
					rules.add(property + ":" + decl.getPropertyValue(property) + ";");
				}
			}
			rule = StringUtils.join(rules, "");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return rule;
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

		return EstimateDirection.isRTL(text);
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

	/**
	 * When there was an error fetching the feed
	 * 
	 */
	public static Date buildDisabledUntil(int errorCount) {
		Date now = new Date();
		int retriesBeforeDisable = 3;

		if (errorCount >= retriesBeforeDisable) {
			int disabledHours = errorCount - retriesBeforeDisable + 1;
			disabledHours = Math.min(24 * 7, disabledHours);
			return DateUtils.addHours(now, disabledHours);
		}
		return now;
	}

	/**
	 * When the feed was refreshed successfully
	 */
	public static Date buildDisabledUntil(Date publishedDate, Long averageEntryInterval, Date defaultRefreshInterval) {
		Date now = new Date();

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
		} else if (averageEntryInterval != null) {
			// use average time between entries to decide when to refresh next, divided by factor
			int factor = 2;

			// not more than 6 hours
			long date = Math.min(DateUtils.addHours(now, 6).getTime(), now.getTime() + averageEntryInterval / factor);

			// not less than default refresh interval
			date = Math.max(defaultRefreshInterval.getTime(), date);

			return new Date(date);
		} else {
			// unknown case, recheck in 24 hours
			return DateUtils.addHours(now, 24);
		}
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

	public static String getFaviconUrl(FeedSubscription subscription, String publicUrl) {
		return removeTrailingSlash(publicUrl) + "/rest/feed/favicon/" + subscription.getId();
	}

	public static String proxyImages(String content, String publicUrl) {
		if (StringUtils.isBlank(content)) {
			return content;
		}

		Document doc = Jsoup.parse(content);
		Elements elements = doc.select("img");
		for (Element element : elements) {
			String href = element.attr("src");
			if (href != null) {
				String proxy = proxyImage(href, publicUrl);
				element.attr("src", proxy);
			}
		}

		return doc.body().html();
	}

	public static String proxyImage(String url, String publicUrl) {
		if (StringUtils.isBlank(url)) {
			return url;
		}
		return removeTrailingSlash(publicUrl) + "/rest/server/proxy?u=" + imageProxyEncoder(url);
	}

	public static String rot13(String msg) {
		StringBuilder message = new StringBuilder();

		for (char c : msg.toCharArray()) {
			if (c >= 'a' && c <= 'm')
				c += 13;
			else if (c >= 'n' && c <= 'z')
				c -= 13;
			else if (c >= 'A' && c <= 'M')
				c += 13;
			else if (c >= 'N' && c <= 'Z')
				c -= 13;
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
