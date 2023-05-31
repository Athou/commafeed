package com.commafeed.backend.feed;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.feed.FeedParser.FeedParserResult;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.urlprovider.FeedURLProvider;
import com.rometools.rome.io.FeedException;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches a feed then parses it
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class FeedFetcher {

	private final FeedParser parser;
	private final HttpGetter getter;
	private final Set<FeedURLProvider> urlProviders;

	public FeedFetcherResult fetch(String feedUrl, boolean extractFeedUrlFromHtml, String lastModified, String eTag, Date lastPublishedDate,
			String lastContentHash) throws FeedException, IOException, NotModifiedException {
		log.debug("Fetching feed {}", feedUrl);

		int timeout = 20000;

		HttpResult result = getter.getBinary(feedUrl, lastModified, eTag, timeout);
		byte[] content = result.getContent();

		FeedParserResult parserResult;
		try {
			parserResult = parser.parse(result.getUrlAfterRedirect(), content);
		} catch (FeedException e) {
			if (extractFeedUrlFromHtml) {
				String extractedUrl = extractFeedUrl(urlProviders, feedUrl, StringUtils.newStringUtf8(result.getContent()));
				if (org.apache.commons.lang3.StringUtils.isNotBlank(extractedUrl)) {
					feedUrl = extractedUrl;

					result = getter.getBinary(extractedUrl, lastModified, eTag, timeout);
					content = result.getContent();
					parserResult = parser.parse(result.getUrlAfterRedirect(), content);
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}

		if (content == null) {
			throw new IOException("Feed content is empty.");
		}

		boolean lastModifiedHeaderValueChanged = !StringUtils.equals(lastModified, result.getLastModifiedSince());
		boolean etagHeaderValueChanged = !StringUtils.equals(eTag, result.getETag());

		String hash = DigestUtils.sha1Hex(content);
		if (lastContentHash != null && lastContentHash.equals(hash)) {
			log.debug("content hash not modified: {}", feedUrl);
			throw new NotModifiedException("content hash not modified",
					lastModifiedHeaderValueChanged ? result.getLastModifiedSince() : null,
					etagHeaderValueChanged ? result.getETag() : null);
		}

		if (lastPublishedDate != null && parserResult.getFeed().getLastPublishedDate() != null
				&& lastPublishedDate.getTime() == parserResult.getFeed().getLastPublishedDate().getTime()) {
			log.debug("publishedDate not modified: {}", feedUrl);
			throw new NotModifiedException("publishedDate not modified",
					lastModifiedHeaderValueChanged ? result.getLastModifiedSince() : null,
					etagHeaderValueChanged ? result.getETag() : null);
		}

		Feed feed = parserResult.getFeed();
		feed.setLastModifiedHeader(result.getLastModifiedSince());
		feed.setEtagHeader(FeedUtils.truncate(result.getETag(), 255));
		feed.setLastContentHash(hash);
		return new FeedFetcherResult(parserResult.getFeed(), parserResult.getEntries(), parserResult.getTitle(),
				result.getUrlAfterRedirect(), result.getDuration());
	}

	private static String extractFeedUrl(Set<FeedURLProvider> urlProviders, String url, String urlContent) {
		for (FeedURLProvider urlProvider : urlProviders) {
			String feedUrl = urlProvider.get(url, urlContent);
			if (feedUrl != null) {
				return feedUrl;
			}
		}

		return null;
	}

	@Value
	public static class FeedFetcherResult {
		Feed feed;
		List<FeedEntry> entries;
		String title;
		String urlAfterRedirect;
		long fetchDuration;
	}

}
