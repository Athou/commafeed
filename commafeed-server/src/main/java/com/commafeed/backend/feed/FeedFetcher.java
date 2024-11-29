package com.commafeed.backend.feed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.Digests;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpRequest;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.feed.parser.FeedParser;
import com.commafeed.backend.feed.parser.FeedParserResult;
import com.commafeed.backend.urlprovider.FeedURLProvider;
import com.rometools.rome.io.FeedException;

import io.quarkus.arc.All;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches a feed then parses it
 */
@Slf4j
@Singleton
public class FeedFetcher {

	private final FeedParser parser;
	private final HttpGetter getter;
	private final List<FeedURLProvider> urlProviders;

	public FeedFetcher(FeedParser parser, HttpGetter getter, @All List<FeedURLProvider> urlProviders) {
		this.parser = parser;
		this.getter = getter;
		this.urlProviders = urlProviders;
	}

	public FeedFetcherResult fetch(String feedUrl, boolean extractFeedUrlFromHtml, String lastModified, String eTag,
			Instant lastPublishedDate, String lastContentHash) throws FeedException, IOException, NotModifiedException {
		log.debug("Fetching feed {}", feedUrl);

		HttpResult result = getter.get(HttpRequest.builder(feedUrl).lastModified(lastModified).eTag(eTag).build());
		byte[] content = result.getContent();

		FeedParserResult parserResult;
		try {
			parserResult = parser.parse(result.getUrlAfterRedirect(), content);
		} catch (FeedException e) {
			if (extractFeedUrlFromHtml) {
				String extractedUrl = extractFeedUrl(urlProviders, feedUrl, new String(result.getContent(), StandardCharsets.UTF_8));
				if (org.apache.commons.lang3.StringUtils.isNotBlank(extractedUrl)) {
					feedUrl = extractedUrl;

					result = getter.get(HttpRequest.builder(extractedUrl).lastModified(lastModified).eTag(eTag).build());
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

		String hash = Digests.sha1Hex(content);
		if (lastContentHash != null && lastContentHash.equals(hash)) {
			log.debug("content hash not modified: {}", feedUrl);
			throw new NotModifiedException("content hash not modified",
					lastModifiedHeaderValueChanged ? result.getLastModifiedSince() : null,
					etagHeaderValueChanged ? result.getETag() : null);
		}

		if (lastPublishedDate != null && lastPublishedDate.equals(parserResult.lastPublishedDate())) {
			log.debug("publishedDate not modified: {}", feedUrl);
			throw new NotModifiedException("publishedDate not modified",
					lastModifiedHeaderValueChanged ? result.getLastModifiedSince() : null,
					etagHeaderValueChanged ? result.getETag() : null);
		}

		return new FeedFetcherResult(parserResult, result.getUrlAfterRedirect(), result.getLastModifiedSince(), result.getETag(), hash,
				result.getValidFor());
	}

	private static String extractFeedUrl(List<FeedURLProvider> urlProviders, String url, String urlContent) {
		for (FeedURLProvider urlProvider : urlProviders) {
			String feedUrl = urlProvider.get(url, urlContent);
			if (feedUrl != null) {
				return feedUrl;
			}
		}

		return null;
	}

	public record FeedFetcherResult(FeedParserResult feed, String urlAfterRedirect, String lastModifiedHeader, String lastETagHeader,
			String contentHash, Duration validFor) {
	}

}
