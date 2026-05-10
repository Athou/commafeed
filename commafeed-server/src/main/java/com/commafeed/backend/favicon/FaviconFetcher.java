package com.commafeed.backend.favicon;

import com.commafeed.backend.model.Feed;

public interface FaviconFetcher {

	Favicon fetch(Feed feed);

}
