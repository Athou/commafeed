package com.commafeed.backend.urlprovider;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

/**
 * Workaround for Youtube channels
 * 
 * converts the channel URL https://www.youtube.com/channel/CHANNEL_ID to the valid feed URL
 * https://www.youtube.com/feeds/videos.xml?channel_id=CHANNEL_ID
 */
@Singleton
public class YoutubeFeedURLProvider implements FeedURLProvider {

	private static final String PREFIX = "https://www.youtube.com/channel/";
	private static final String REPLACEMENT_PREFIX = "https://www.youtube.com/feeds/videos.xml?channel_id=";

	@Override
	public String get(String url, String urlContent) {
		if (!StringUtils.startsWithIgnoreCase(url, PREFIX)) {
			return null;
		}

		return REPLACEMENT_PREFIX + url.substring(PREFIX.length());
	}

}
