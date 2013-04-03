package com.commafeed.backend.feeds;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.model.Feed;
import com.sun.syndication.io.FeedException;

@Stateless
public class FeedFetcher {

	private static Logger log = LoggerFactory.getLogger(FeedFetcher.class);

	@Inject
	FeedParser parser;

	@Inject
	HttpGetter getter;

	public Feed fetch(String feedUrl) throws FeedException {
		log.debug("Fetching feed {}", feedUrl);
		Feed feed = null;

		try {
			String content = getter.get(feedUrl);
			String extractedUrl = extractFeedUrl(content);
			if (extractedUrl != null) {
				content = getter.get(extractedUrl);
				feedUrl = extractedUrl;
			}
			feed = parser.parse(feedUrl, content);
		} catch (Exception e) {
			throw new FeedException(e.getMessage(), e);
		}
		return feed;
	}

	private String extractFeedUrl(String html) {
		String foundUrl = null;

		Document doc = Jsoup.parse(html);
		String root = doc.children().get(0).tagName();
		if ("html".equals(root)) {
			Elements rss = doc.select("link[type=application/rss+xml]");
			Elements atom = doc.select("link[type=application/atom+xml]");
			if (!rss.isEmpty()) {
				foundUrl = rss.get(0).attr("abs:href").toString();
			} else if (!atom.isEmpty()) {
				foundUrl = atom.get(0).attr("abs:href").toString();
			}
		}
		return foundUrl;
	}
}
