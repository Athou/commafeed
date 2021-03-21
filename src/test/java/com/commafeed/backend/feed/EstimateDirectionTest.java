package com.commafeed.backend.feed;

import static org.junit.jupiter.api.Assertions.*;
import static com.commafeed.backend.feed.EstimateDirection.isRTL;

import org.junit.jupiter.api.Test;


/**
 * These tests are copied and simplified from GWT
 * https://github.com/google-web-toolkit/gwt/blob/master/user/test/com/google/gwt/i18n/shared/BidiUtilsTest.java Released under Apache 2.0
 * license, credit of it goes to Google and please use GWT wherever possible instead of this
 */
public class EstimateDirectionTest {

	@Test
	public void testEstimateDirection() {
		assertFalse(isRTL(""));
		assertFalse(isRTL(" "));
		assertFalse(isRTL("! (...)"));
		assertFalse(isRTL("Pure Ascii content"));
		assertFalse(isRTL("-17.0%"));
		assertFalse(isRTL("http://foo/bar/"));
		assertFalse(isRTL("http://foo/bar/?s=\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0"
				+ "\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0"
				+ "\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0"));
		assertTrue(isRTL("\u05d0"));
		assertTrue(isRTL("\u05d0"));
		assertTrue(isRTL("9 \u05d0 -> 17.5, 23, 45, 19"));
		assertTrue(isRTL("http://foo/bar/ \u05d0 http://foo2/bar2/ http://foo3/bar3/"));
		assertTrue(isRTL("\u05d0\u05d9\u05df \u05de\u05de\u05e9 "
				+ "\u05de\u05d4 \u05dc\u05e8\u05d0\u05d5\u05ea: "
				+ "\u05dc\u05d0 \u05e6\u05d9\u05dc\u05de\u05ea\u05d9 "
				+ "\u05d4\u05e8\u05d1\u05d4 \u05d5\u05d2\u05dd \u05d0"
				+ "\u05dd \u05d4\u05d9\u05d9\u05ea\u05d9 \u05de\u05e6\u05dc"
				+ "\u05dd, \u05d4\u05d9\u05d4 \u05e9\u05dd"));
		assertTrue(isRTL("\u05db\u05d0\u05df - http://geek.co.il/gallery/v/2007-06"
				+ " - \u05d0\u05d9\u05df \u05de\u05de\u05e9 \u05de\u05d4 "
				+ "\u05dc\u05e8\u05d0\u05d5\u05ea: \u05dc\u05d0 \u05e6"
				+ "\u05d9\u05dc\u05de\u05ea\u05d9 \u05d4\u05e8\u05d1\u05d4 "
				+ "\u05d5\u05d2\u05dd \u05d0\u05dd \u05d4\u05d9\u05d9\u05ea"
				+ "\u05d9 \u05de\u05e6\u05dc\u05dd, \u05d4\u05d9\u05d4 "
				+ "\u05e9\u05dd \u05d1\u05e2\u05d9\u05e7\u05e8 \u05d4\u05e8"
				+ "\u05d1\u05d4 \u05d0\u05e0\u05e9\u05d9\u05dd. \u05de"
				+ "\u05d4 \u05e9\u05db\u05df - \u05d0\u05e4\u05e9\u05e8 "
				+ "\u05dc\u05e0\u05e6\u05dc \u05d0\u05ea \u05d4\u05d4 "
				+ "\u05d3\u05d6\u05de\u05e0\u05d5\u05ea \u05dc\u05d4\u05e1"
				+ "\u05ea\u05db\u05dc \u05e2\u05dc \u05db\u05de\u05d4 "
				+ "\u05ea\u05de\u05d5\u05e0\u05d5\u05ea \u05de\u05e9\u05e2"
				+ "\u05e9\u05e2\u05d5\u05ea \u05d9\u05e9\u05e0\u05d5\u05ea "
				+ "\u05d9\u05d5\u05ea\u05e8 \u05e9\u05d9\u05e9 \u05dc"
				+ "\u05d9 \u05d1\u05d0\u05ea\u05e8"));
		assertTrue(isRTL("CAPTCHA \u05de\u05e9\u05d5\u05db\u05dc\u05dc "
				+ "\u05de\u05d3\u05d9?"));
		assertTrue(isRTL("Yes Prime Minister \u05e2\u05d3\u05db\u05d5\u05df. "
				+ "\u05e9\u05d0\u05dc\u05d5 \u05d0\u05d5\u05ea\u05d9 "
				+ "\u05de\u05d4 \u05d0\u05e0\u05d9 \u05e8\u05d5\u05e6"
				+ "\u05d4 \u05de\u05ea\u05e0\u05d4 \u05dc\u05d7\u05d2"));
		assertTrue(isRTL("17.4.02 \u05e9\u05e2\u05d4:13-20 .15-00 .\u05dc\u05d0 "
				+ "\u05d4\u05d9\u05d9\u05ea\u05d9 \u05db\u05d0\u05df."));
		assertTrue(isRTL("5710 5720 5730. \u05d4\u05d3\u05dc\u05ea. "
				+ "\u05d4\u05e0\u05e9\u05d9\u05e7\u05d4"));
		assertTrue(isRTL("\u05d4\u05d3\u05dc\u05ea http://www.google.com "
				+ "http://www.gmail.com"));
	}
}