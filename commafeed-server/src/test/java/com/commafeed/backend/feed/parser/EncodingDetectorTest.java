package com.commafeed.backend.feed.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EncodingDetectorTest {

	EncodingDetector encodingDetector = new EncodingDetector();

	@Test
	void testExtractDeclaredEncoding() {
		Assertions.assertNull(encodingDetector.extractDeclaredEncoding("<?xml ?>".getBytes()));
		Assertions.assertNull(encodingDetector.extractDeclaredEncoding("<feed></feed>".getBytes()));
		Assertions.assertEquals("UTF-8", encodingDetector.extractDeclaredEncoding("<?xml encoding=\"UTF-8\" ?>".getBytes()));
		Assertions.assertEquals("UTF-8", encodingDetector.extractDeclaredEncoding("<?xml encoding='UTF-8' ?>".getBytes()));
		Assertions.assertEquals("UTF-8", encodingDetector.extractDeclaredEncoding("<?xml encoding='UTF-8'?>".getBytes()));
	}

}