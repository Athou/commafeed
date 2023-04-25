package com.commafeed.backend.urlprovider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class InPageReferenceFeedURLProvider implements FeedURLProvider {

	@Override
	public String get(String url, String urlContent) {
		String foundUrl = null;

		Document doc = Jsoup.parse(urlContent, url);
		String root = doc.children().get(0).tagName();
		if ("html".equals(root)) {
			Elements atom = doc.select("link[type=application/atom+xml]");
			Elements rss = doc.select("link[type=application/rss+xml]");
			if (!atom.isEmpty()) {
				foundUrl = atom.get(0).attr("abs:href");
			} else if (!rss.isEmpty()) {
				foundUrl = rss.get(0).attr("abs:href");
			}
		}

		return foundUrl;
	}

}
