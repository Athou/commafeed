package com.commafeed.backend.feed;

import org.apache.hc.client5.http.utils.Base64;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ImageProxyUrl {

	public static String encode(String url) {
		return Base64.encodeBase64String(rot13(url).getBytes());
	}

	public static String decode(String code) {
		return rot13(new String(Base64.decodeBase64(code)));
	}

	private static String rot13(String msg) {
		StringBuilder message = new StringBuilder();

		for (char c : msg.toCharArray()) {
			if (c >= 'a' && c <= 'm') {
				c += 13;
			} else if (c >= 'n' && c <= 'z') {
				c -= 13;
			} else if (c >= 'A' && c <= 'M') {
				c += 13;
			} else if (c >= 'N' && c <= 'Z') {
				c -= 13;
			}
			message.append(c);
		}

		return message.toString();
	}

}
