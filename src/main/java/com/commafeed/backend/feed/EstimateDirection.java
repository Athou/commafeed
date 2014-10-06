package com.commafeed.backend.feed;

import java.util.regex.Pattern;

/**
 * This code is copied and simplified from GWT
 * https://github.com/google-web-toolkit/gwt/blob/master/user/src/com/google/gwt/i18n/shared/BidiUtils.java Released under Apache 2.0
 * license, credit of it goes to Google and please use GWT wherever possible instead of this
 */
class EstimateDirection {
	private static final float RTL_DETECTION_THRESHOLD = 0.40f;

	private static final String LTR_CHARS = "A-Za-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02B8\u0300-\u0590\u0800-\u1FFF"
			+ "\u2C00-\uFB1C\uFDFE-\uFE6F\uFEFD-\uFFFF";
	private static final String RTL_CHARS = "\u0591-\u07FF\uFB1D-\uFDFD\uFE70-\uFEFC";

	private static final Pattern WORD_SEPARATOR_RE = Pattern.compile("\\s+");
	private static final Pattern FIRST_STRONG_IS_RTL_RE = Pattern.compile("^[^" + LTR_CHARS + "]*[" + RTL_CHARS + ']');
	private static final Pattern IS_REQUIRED_LTR_RE = Pattern.compile("^http://.*");
	private static final Pattern HAS_ANY_LTR_RE = Pattern.compile("[" + LTR_CHARS + ']');

	private static boolean startsWithRtl(String str) {
		return FIRST_STRONG_IS_RTL_RE.matcher(str).matches();
	}

	private static boolean hasAnyLtr(String str) {
		return HAS_ANY_LTR_RE.matcher(str).matches();
	}

	static boolean isRTL(String str) {
		int rtlCount = 0;
		int total = 0;
		String[] tokens = WORD_SEPARATOR_RE.split(str, 20); // limit splits to 20, usually enough
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (startsWithRtl(token)) {
				rtlCount++;
				total++;
			} else if (IS_REQUIRED_LTR_RE.matcher(token).matches()) {
				// do nothing
			} else if (hasAnyLtr(token)) {
				total++;
			}
		}

		return total == 0 ? false : ((float) rtlCount / total > RTL_DETECTION_THRESHOLD ? true : false);
	}
}
