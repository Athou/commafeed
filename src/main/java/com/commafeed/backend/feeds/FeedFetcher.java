package com.commafeed.backend.feeds;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
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

	public FetchedFeed fetch(String feedUrl, boolean extractFeedUrlFromHtml,
			String lastModified, String eTag, Date lastPublishedDate,
			String lastContentHash) throws FeedException,
			ClientProtocolException, IOException, NotModifiedException {
		log.debug("Fetching feed {}", feedUrl);
		FetchedFeed fetchedFeed = null;

		int timeout = 20000;
		HttpResult result = getter.getBinary(feedUrl, lastModified, eTag, timeout);
		if (extractFeedUrlFromHtml) {
			String extractedUrl = extractFeedUrl(
					StringUtils.newStringUtf8(result.getContent()), feedUrl);
			if (org.apache.commons.lang.StringUtils.isNotBlank(extractedUrl)) {
				result = getter.getBinary(extractedUrl, lastModified, eTag, timeout);
				feedUrl = extractedUrl;
			}
		}
		byte[] content = result.getContent();

		if (content == null) {
			throw new IOException("Feed content is empty.");
		}

		String hash = DigestUtils.sha1Hex(content);
		if (lastContentHash != null && hash != null
				&& lastContentHash.equals(hash)) {
			log.debug("content hash not modified: {}", feedUrl);
			throw new NotModifiedException();
		}

		fetchedFeed = parser.parse(feedUrl, content);

		if (lastPublishedDate != null
				&& fetchedFeed.getFeed().getLastPublishedDate() != null
				&& lastPublishedDate.getTime() == fetchedFeed.getFeed()
						.getLastPublishedDate().getTime()) {
			log.debug("publishedDate not modified: {}", feedUrl);
			throw new NotModifiedException();
		}

		Feed feed = fetchedFeed.getFeed();
		feed.setLastModifiedHeader(result.getLastModifiedSince());
		feed.setEtagHeader(FeedUtils.truncate(result.geteTag(), 255));
		feed.setLastContentHash(hash);
		fetchedFeed.setFetchDuration(result.getDuration());
		return fetchedFeed;
	}

	private String extractFeedUrl(String html, String baseUri) {
		String foundUrl = null;

		Document doc = Jsoup.parse(html, baseUri);
		String root = doc.children().get(0).tagName();
		if ("html".equals(root)) {
			Elements atom = doc.select("link[type=application/atom+xml]");
			Elements rss = doc.select("link[type=application/rss+xml]");
			if (!atom.isEmpty()) {
				foundUrl = atom.get(0).attr("abs:href").toString();
			} else if (!rss.isEmpty()) {
				foundUrl = rss.get(0).attr("abs:href").toString();
			}
		}
		return foundUrl;
	}
}
