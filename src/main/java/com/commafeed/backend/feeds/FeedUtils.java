package com.commafeed.backend.feeds;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;
import org.mozilla.universalchardet.UniversalDetector;

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

	public static String trimInvalidXmlCharacters(String xml) {
		if (StringUtils.isBlank(xml)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < xml.length(); i++) {
			char c = xml.charAt(i);
			if (c >= 20 || c == 0x9 || c == 0xA || c == 0xD) {
				sb.append(c);
			}
		}
		return sb.toString();
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
