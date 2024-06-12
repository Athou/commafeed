package com.commafeed.backend;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("deprecation")
public class Digests {

	public static String sha1Hex(byte[] input) {
		return hashBytesToHex(Hashing.sha1(), input);
	}

	public static String sha1Hex(String input) {
		return hashBytesToHex(Hashing.sha1(), input.getBytes(StandardCharsets.UTF_8));
	}

	public static String md5Hex(String input) {
		return hashBytesToHex(Hashing.md5(), input.getBytes(StandardCharsets.UTF_8));
	}

	private static String hashBytesToHex(HashFunction function, byte[] input) {
		return function.hashBytes(input).toString();
	}
}
