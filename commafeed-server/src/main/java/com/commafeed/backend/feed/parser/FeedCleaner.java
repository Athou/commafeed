package com.commafeed.backend.feed.parser;

import java.util.Collection;
import java.util.regex.Pattern;

import jakarta.inject.Singleton;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Verifier;

@Singleton
public class FeedCleaner {

	private static final Pattern DOCTYPE_PATTERN = Pattern.compile("<!DOCTYPE[^>]*>", Pattern.CASE_INSENSITIVE);

	public String clean(String xml) {
		xml = removeCharactersBeforeFirstXmlTag(xml);
		xml = removeInvalidXmlCharacters(xml);
		xml = replaceHtmlEntitiesWithNumericEntities(xml);
		xml = removeDoctypeDeclarations(xml);
		return xml;
	}

	String removeCharactersBeforeFirstXmlTag(String xml) {
		if (StringUtils.isBlank(xml)) {
			return null;
		}

		int pos = xml.indexOf('<');
		return pos < 0 ? null : xml.substring(pos);
	}

	String removeInvalidXmlCharacters(String xml) {
		if (StringUtils.isBlank(xml)) {
			return null;
		}

		return xml.codePoints()
				.filter(Verifier::isXMLCharacter)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	// https://stackoverflow.com/a/40836618
	String replaceHtmlEntitiesWithNumericEntities(String source) {
		if (StringUtils.isBlank(source)) {
			return null;
		}

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

	String removeDoctypeDeclarations(String xml) {
		if (StringUtils.isBlank(xml)) {
			return null;
		}

		return DOCTYPE_PATTERN.matcher(xml).replaceAll("");
	}

}
