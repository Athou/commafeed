package com.commafeed.backend.feed;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import com.google.common.primitives.Bytes;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ImageProxyUrl {

	private static final SecretKey KEY;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final int GCM_IV_LENGTH = 12;
	private static final int GCM_TAG_LENGTH = 128;

	static {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256, SECURE_RANDOM);
			KEY = keyGen.generateKey();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to generate AES key", e);
		}
	}

	public static String encode(String url) {
		try {
			byte[] iv = new byte[GCM_IV_LENGTH];
			SECURE_RANDOM.nextBytes(iv);

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
			byte[] encrypted = cipher.doFinal(url.getBytes(StandardCharsets.UTF_8));

			byte[] combined = Bytes.concat(iv, encrypted);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to encode URL", e);
		}
	}

	public static String decode(String code) {
		try {
			byte[] combined = Base64.getUrlDecoder().decode(code);

			byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
			byte[] encrypted = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
			byte[] decrypted = cipher.doFinal(encrypted);

			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to decode URL", e);
		}
	}

}
