package com.commafeed.backend.feed;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomUtils;

import com.google.common.primitives.Bytes;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ImageProxyUrl {

	private static final int GCM_IV_LENGTH = 12;
	private static final int GCM_TAG_LENGTH = 128;

	private static SecretKey key;

	public static void generateKey() {
		key = new SecretKeySpec(RandomUtils.secure().randomBytes(32), "AES");
	}

	public static String encode(String url) {
		if (key == null) {
			throw new IllegalStateException("Key not initialized");
		}

		try {
			byte[] iv = RandomUtils.secure().randomBytes(GCM_IV_LENGTH);

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
			byte[] encrypted = cipher.doFinal(url.getBytes(StandardCharsets.UTF_8));

			byte[] combined = Bytes.concat(iv, encrypted);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to encode URL", e);
		}
	}

	public static String decode(String code) {
		if (key == null) {
			throw new IllegalStateException("Key not initialized");
		}

		try {
			byte[] combined = Base64.getUrlDecoder().decode(code);

			byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
			byte[] encrypted = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
			byte[] decrypted = cipher.doFinal(encrypted);

			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to decode URL", e);
		}
	}

}
