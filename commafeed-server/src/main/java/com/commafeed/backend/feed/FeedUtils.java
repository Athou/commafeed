package com.commafeed.backend.feed;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.commafeed.backend.feed.parser.TextDirectionDetector;
import com.commafeed.backend.model.FeedSubscription;

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

}
