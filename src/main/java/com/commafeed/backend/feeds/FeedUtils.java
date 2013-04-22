package com.commafeed.backend.feeds;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;

public class FeedUtils {

	public static String handleContent(String content) {
		if (StringUtils.isNotBlank(content)) {
			content = trimUnicodeSurrogateCharacters(content);
			Whitelist whitelist = Whitelist.relaxed();
			whitelist.addEnforcedAttribute("a", "target", "_blank");

			whitelist.addTags("iframe");
			whitelist.addAttributes("iframe", "src", "height", "width",
					"allowfullscreen", "frameborder");

			content = Jsoup.clean(content, "", whitelist,
					new OutputSettings().escapeMode(EscapeMode.base));
		}
		return content;
	}

	public static String trimUnicodeSurrogateCharacters(String text) {
		if (StringUtils.isBlank(text)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (!Character.isHighSurrogate(ch) && !Character.isLowSurrogate(ch)) {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
}
