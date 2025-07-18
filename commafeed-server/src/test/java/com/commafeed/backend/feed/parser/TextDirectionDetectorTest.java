package com.commafeed.backend.feed.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TextDirectionDetectorTest {

	@Test
	void testEstimateDirection() {
		Assertions.assertEquals(TextDirectionDetector.Direction.LEFT_TO_RIGHT, TextDirectionDetector.detect(""));
		Assertions.assertEquals(TextDirectionDetector.Direction.LEFT_TO_RIGHT, TextDirectionDetector.detect(" "));
		Assertions.assertEquals(TextDirectionDetector.Direction.LEFT_TO_RIGHT, TextDirectionDetector.detect("! (...)"));
		Assertions.assertEquals(TextDirectionDetector.Direction.LEFT_TO_RIGHT, TextDirectionDetector.detect("Pure Ascii content"));
		Assertions.assertEquals(TextDirectionDetector.Direction.LEFT_TO_RIGHT, TextDirectionDetector.detect("-17.0%"));
		Assertions.assertEquals(TextDirectionDetector.Direction.LEFT_TO_RIGHT, TextDirectionDetector.detect("http://foo/bar/"));
		Assertions.assertEquals(TextDirectionDetector.Direction.LEFT_TO_RIGHT,
				TextDirectionDetector.detect("http://foo/bar/?s=\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0"
						+ "\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0" + "\u05d0\u05d0\u05d0\u05d0\u05d0\u05d0"));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT, TextDirectionDetector.detect("\u05d0"));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT, TextDirectionDetector.detect("\u05d0"));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT,
				TextDirectionDetector.detect("http://foo/bar/ \u05d0 http://foo2/bar2/ http://foo3/bar3/"));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT,
				TextDirectionDetector.detect("\u05d0\u05d9\u05df \u05de\u05de\u05e9 " + "\u05de\u05d4 \u05dc\u05e8\u05d0\u05d5\u05ea: "
						+ "\u05dc\u05d0 \u05e6\u05d9\u05dc\u05de\u05ea\u05d9 " + "\u05d4\u05e8\u05d1\u05d4 \u05d5\u05d2\u05dd \u05d0"
						+ "\u05dd \u05d4\u05d9\u05d9\u05ea\u05d9 \u05de\u05e6\u05dc" + "\u05dd, \u05d4\u05d9\u05d4 \u05e9\u05dd"));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT,
				TextDirectionDetector.detect("\u05db\u05d0\u05df - http://geek.co.il/gallery/v/2007-06"
						+ " - \u05d0\u05d9\u05df \u05de\u05de\u05e9 \u05de\u05d4 " + "\u05dc\u05e8\u05d0\u05d5\u05ea: \u05dc\u05d0 \u05e6"
						+ "\u05d9\u05dc\u05de\u05ea\u05d9 \u05d4\u05e8\u05d1\u05d4 "
						+ "\u05d5\u05d2\u05dd \u05d0\u05dd \u05d4\u05d9\u05d9\u05ea"
						+ "\u05d9 \u05de\u05e6\u05dc\u05dd, \u05d4\u05d9\u05d4 "
						+ "\u05e9\u05dd \u05d1\u05e2\u05d9\u05e7\u05e8 \u05d4\u05e8" + "\u05d1\u05d4 \u05d0\u05e0\u05e9\u05d9\u05dd. \u05de"
						+ "\u05d4 \u05e9\u05db\u05df - \u05d0\u05e4\u05e9\u05e8 " + "\u05dc\u05e0\u05e6\u05dc \u05d0\u05ea \u05d4\u05d4 "
						+ "\u05d3\u05d6\u05de\u05e0\u05d5\u05ea \u05dc\u05d4\u05e1" + "\u05ea\u05db\u05dc \u05e2\u05dc \u05db\u05de\u05d4 "
						+ "\u05ea\u05de\u05d5\u05e0\u05d5\u05ea \u05de\u05e9\u05e2"
						+ "\u05e9\u05e2\u05d5\u05ea \u05d9\u05e9\u05e0\u05d5\u05ea " + "\u05d9\u05d5\u05ea\u05e8 \u05e9\u05d9\u05e9 \u05dc"
						+ "\u05d9 \u05d1\u05d0\u05ea\u05e8"));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT,
				TextDirectionDetector.detect("CAPTCHA \u05de\u05e9\u05d5\u05db\u05dc\u05dc " + "\u05de\u05d3\u05d9?"));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT,
				TextDirectionDetector.detect("Yes Prime Minister \u05e2\u05d3\u05db\u05d5\u05df. "
						+ "\u05e9\u05d0\u05dc\u05d5 \u05d0\u05d5\u05ea\u05d9 " + "\u05de\u05d4 \u05d0\u05e0\u05d9 \u05e8\u05d5\u05e6"
						+ "\u05d4 \u05de\u05ea\u05e0\u05d4 \u05dc\u05d7\u05d2"));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT, TextDirectionDetector
				.detect("17.4.02 \u05e9\u05e2\u05d4:13-20 .15-00 .\u05dc\u05d0 " + "\u05d4\u05d9\u05d9\u05ea\u05d9 \u05db\u05d0\u05df."));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT,
				TextDirectionDetector.detect("5710 5720 5730. \u05d4\u05d3\u05dc\u05ea. " + "\u05d4\u05e0\u05e9\u05d9\u05e7\u05d4"));
		Assertions.assertEquals(TextDirectionDetector.Direction.RIGHT_TO_LEFT,
				TextDirectionDetector.detect("\u05d4\u05d3\u05dc\u05ea http://www.google.com " + "http://www.gmail.com"));
	}

}