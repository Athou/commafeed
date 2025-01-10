package com.commafeed.backend.feed.parser;

import java.util.Collection;
import java.util.regex.Pattern;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.commons.lang3.StringUtils;

import jakarta.inject.Singleton;

@Singleton
class FeedCleaner {

	private static final Pattern DOCTYPE_PATTERN = Pattern.compile("<!DOCTYPE[^>]*>", Pattern.CASE_INSENSITIVE);

	public String trimInvalidXmlCharacters(String xml) {
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

	// https://stackoverflow.com/a/40836618
	public String replaceHtmlEntitiesWithNumericEntities(String source) {
		// Create a buffer sufficiently large that re-allocations are minimized.
		StringBuilder sb = new StringBuilder(source.length() << 1);

		Collection<Emit> emits = Trie.builder().ignoreOverlaps().addKeywords(HtmlEntities.HTML_ENTITIES).build().parseText(source);

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

	public String removeDoctypeDeclarations(String xml) {
		return DOCTYPE_PATTERN.matcher(xml).replaceAll("");
	}

}
