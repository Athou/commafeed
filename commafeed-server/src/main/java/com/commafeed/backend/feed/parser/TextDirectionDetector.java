package com.commafeed.backend.feed.parser;

import java.text.Bidi;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

public class TextDirectionDetector {

	private static final Pattern WORDS_PATTERN = Pattern.compile("\\s+");
	private static final Pattern URL_PATTERN = Pattern.compile("^https?://.*");

	private static final double RTL_THRESHOLD = 0.4D;

	public enum Direction {
		LEFT_TO_RIGHT, RIGHT_TO_LEFT
	}

	public static Direction detect(String input) {
		if (input == null || input.isBlank()) {
			return Direction.LEFT_TO_RIGHT;
		}

		long rtl = 0;
		long total = 0;
		for (String token : WORDS_PATTERN.split(input)) {
			// skip urls
			if (URL_PATTERN.matcher(token).matches()) {
				continue;
			}

			// skip numbers
			if (NumberUtils.isCreatable(token)) {
				continue;
			}

			boolean requiresBidi = Bidi.requiresBidi(token.toCharArray(), 0, token.length());
			if (requiresBidi) {
				Bidi bidi = new Bidi(token, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
				if (bidi.getBaseLevel() == 1) {
					rtl++;
				}
			}

			total++;
		}

		if (total == 0) {
			return Direction.LEFT_TO_RIGHT;
		}

		double ratio = (double) rtl / total;
		return ratio > RTL_THRESHOLD ? Direction.RIGHT_TO_LEFT : Direction.LEFT_TO_RIGHT;
	}

}
