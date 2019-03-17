package com.commafeed.backend.urlprovider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Workaround for Youtube channels
 * 
 * converts the channel URL https://www.youtube.com/channel/CHANNEL_ID to the valid feed URL
 * https://www.youtube.com/feeds/videos.xml?channel_id=CHANNEL_ID
 */
public class YoutubeFeedURLProvider implements FeedURLProvider {

	private static final Pattern REGEXP = Pattern.compile("(.*\\byoutube\\.com)\\/channel\\/([^\\/]+)", Pattern.CASE_INSENSITIVE);

	@Override
	public String get(String url, String urlContent) {
		Matcher matcher = REGEXP.matcher(url);
		return matcher.find() ? matcher.group(1) + "/feeds/videos.xml?channel_id=" + matcher.group(2) : null;
	}

}
