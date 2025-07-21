package com.commafeed.backend.feed;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

	public static String truncate(String string, int length) {
		return StringUtils.truncate(string, length);
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

		return "rest/server/proxy?u=" + ImageProxyUrl.encode(url);
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
