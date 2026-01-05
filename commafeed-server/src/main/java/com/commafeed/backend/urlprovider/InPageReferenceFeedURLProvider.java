package com.commafeed.backend.urlprovider;

import java.util.List;
import java.util.stream.Stream;

import jakarta.inject.Singleton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Singleton
public class InPageReferenceFeedURLProvider implements FeedURLProvider {

	@Override
	public List<String> get(String url, String urlContent) {
		Document doc = Jsoup.parse(urlContent, url);
		if (!"html".equals(doc.children().getFirst().tagName())) {
			return List.of();
		}
		return Stream.concat(doc.select("link[type=application/atom+xml]").stream(), doc.select("link[type=application/rss+xml]").stream())
				.map(node -> node.attr("abs:href"))
				.toList();
	}

}
