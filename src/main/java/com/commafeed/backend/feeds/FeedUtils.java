package com.commafeed.backend.feeds;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;
import org.mozilla.universalchardet.UniversalDetector;

import com.commafeed.backend.model.FeedEntry;

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
		} else if (encoding.equalsIgnoreCase("ISO-8859-1")) {
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

	public static long average(List<FeedEntry> entries) {
		SummaryStatistics stats = new SummaryStatistics();
		for (int i = 0; i < entries.size() - 1; i++) {
			long diff = Math.abs(entries.get(i).getUpdated().getTime()
					- entries.get(i + 1).getUpdated().getTime());
			stats.addValue(diff);
		}
		return (long) stats.getMean();
	}
}
