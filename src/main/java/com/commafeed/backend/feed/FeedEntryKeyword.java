package com.commafeed.backend.feed;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;

/**
 * A keyword used in a search query
 */
@Getter
@RequiredArgsConstructor
public class FeedEntryKeyword {

	public static enum Mode {
		INCLUDE, EXCLUDE;
	}

	private final String keyword;
	private final Mode mode;

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
