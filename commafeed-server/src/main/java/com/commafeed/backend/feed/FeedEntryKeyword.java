package com.commafeed.backend.feed;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * A keyword used in a search query
 */
public record FeedEntryKeyword(String keyword, Mode mode) {

	public enum Mode {
		INCLUDE, EXCLUDE
	}

	public static List<FeedEntryKeyword> fromQueryString(String keywords) {
		List<FeedEntryKeyword> list = new ArrayList<>();
		if (keywords != null) {
			for (String keyword : StringUtils.split(keywords)) {
				boolean not = false;
				if (keyword.startsWith("-") || keyword.startsWith("!")) {
					not = true;
					keyword = keyword.substring(1);
				}
				list.add(new FeedEntryKeyword(keyword, not ? Mode.EXCLUDE : Mode.INCLUDE));
			}
		}
		return list;
	}
}
