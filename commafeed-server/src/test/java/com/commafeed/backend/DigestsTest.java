package com.commafeed.backend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DigestsTest {

	@Test
	void sha1Hex() {
		Assertions.assertEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d", Digests.sha1Hex("hello"));
	}

	@Test
	void md5Hex() {
		Assertions.assertEquals("5d41402abc4b2a76b9719d911017c592", Digests.md5Hex("hello"));
	}

}