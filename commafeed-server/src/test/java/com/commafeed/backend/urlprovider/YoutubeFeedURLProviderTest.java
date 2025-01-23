package com.commafeed.backend.urlprovider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class YoutubeFeedURLProviderTest {

	private final YoutubeFeedURLProvider provider = new YoutubeFeedURLProvider();

	@Test
	void matchesYoutubeChannelURL() {
		Assertions.assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=abc",
				provider.get("https://www.youtube.com/channel/abc", null));
	}

	@Test
	void doesNotmatchYoutubeChannelURL() {
		Assertions.assertNull(provider.get("https://www.anothersite.com/channel/abc", null));
		Assertions.assertNull(provider.get("https://www.youtube.com/user/abc", null));
	}

}