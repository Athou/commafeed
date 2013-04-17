package com.commafeed.backend.feeds;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.model.Feed;
import com.sun.syndication.io.FeedException;

public class FeedFetcher {

	private static Logger log = LoggerFactory.getLogger(FeedFetcher.class);

	@Inject
	FeedParser parser;

	@Inject
	HttpGetter getter;

	public Feed fetch(String feedUrl, boolean extractFeedUrlFromHtml,
			String lastModified, String eTag) throws FeedException,
			ClientProtocolException, IOException, NotModifiedException {
		log.debug("Fetching feed {}", feedUrl);
		Feed feed = null;

		HttpResult result = getter.getBinary(feedUrl, lastModified, eTag);
		if (extractFeedUrlFromHtml) {
			String extractedUrl = extractFeedUrl(StringUtils
					.newStringUtf8(result.getContent()), feedUrl);
			if (org.apache.commons.lang.StringUtils.isNotBlank(extractedUrl)) {
				result = getter.getBinary(extractedUrl, lastModified, eTag);
				feedUrl = extractedUrl;
			}
		}
		feed = parser.parse(feedUrl, result.getContent());
		feed.setLastModifiedHeader(result.getLastModifiedSince());
		feed.setEtagHeader(result.geteTag());
		return feed;
	}

	private String extractFeedUrl(String html, String baseUri) {
		String foundUrl = null;

		Document doc = Jsoup.parse(html, baseUri);
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
