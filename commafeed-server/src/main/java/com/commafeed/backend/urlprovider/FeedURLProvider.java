package com.commafeed.backend.urlprovider;

import java.util.List;

/**
 * Tries to find a feed url given the url and page content
 */
public interface FeedURLProvider {

	List<String> get(String url, String urlContent);

}
