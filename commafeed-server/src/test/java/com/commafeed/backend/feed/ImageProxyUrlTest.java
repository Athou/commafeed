package com.commafeed.backend.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ImageProxyUrlTest {

	@ParameterizedTest
	@ValueSource(
			strings = {
					// simple URL
					"http://example.com/image.jpg",

					// URL with query parameters
					"https://example.com/image.png?width=100&height=200",

					// URL with special characters
					"https://example.com/path/image.gif?param=value&other=test#fragment",

					// URL with non-ascii characters
					"https://example.com/image-ñáéíóú.jpg",

					// blank URL
					"",

					// URL with port number
					"http://localhost:8080/images/photo.jpg",

					// long URL
					"https://very-long-domain-name-example.com/very/long/path/to/image/file/with/many/segments/image.jpg?param1=value1&param2=value2&param3=value3",

					// URL with mixed case
					"HTTPS://EXAMPLE.COM/Image.JPG",

					// URL with port number
					"https://example123.com/image123.jpg?id=456789", })
	void testEncodingDecoding(String originalUrl) {
		String encoded = ImageProxyUrl.encode(originalUrl);
		String decoded = ImageProxyUrl.decode(encoded);

		Assertions.assertEquals(originalUrl, decoded);
	}

	@Test
	void encodeProducesDifferentResultForDifferentUrls() {
		String url1 = "https://example.com/image1.jpg";
		String url2 = "https://example.com/image2.jpg";

		String encoded1 = ImageProxyUrl.encode(url1);
		String encoded2 = ImageProxyUrl.encode(url2);

		Assertions.assertNotEquals(encoded1, encoded2);
	}

}