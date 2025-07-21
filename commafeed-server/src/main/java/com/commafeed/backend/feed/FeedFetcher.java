package com.commafeed.backend.feed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.commafeed.backend.Digests;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HostNotAllowedException;
import com.commafeed.backend.HttpGetter.HttpRequest;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.HttpGetter.SchemeNotAllowedException;
import com.commafeed.backend.HttpGetter.TooManyRequestsException;
import com.commafeed.backend.feed.parser.FeedParser;
import com.commafeed.backend.feed.parser.FeedParser.FeedParsingException;
import com.commafeed.backend.feed.parser.FeedParserResult;
import com.commafeed.backend.urlprovider.FeedURLProvider;

import io.quarkus.arc.All;
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
			Instant lastPublishedDate, String lastContentHash) throws FeedParsingException, IOException, NotModifiedException,
			TooManyRequestsException, SchemeNotAllowedException, HostNotAllowedException, NoFeedFoundException {
		log.debug("Fetching feed {}", feedUrl);

		HttpResult result = getter.get(HttpRequest.builder(feedUrl).lastModified(lastModified).eTag(eTag).build());
		byte[] content = result.getContent();

		FeedParserResult parserResult;
		try {
			parserResult = parser.parse(result.getUrlAfterRedirect(), content);
		} catch (FeedParsingException e) {
			if (extractFeedUrlFromHtml) {
				String extractedUrl = extractFeedUrl(urlProviders, feedUrl, new String(result.getContent(), StandardCharsets.UTF_8));
				if (StringUtils.isNotBlank(extractedUrl)) {
					feedUrl = extractedUrl;

					result = getter.get(HttpRequest.builder(extractedUrl).lastModified(lastModified).eTag(eTag).build());
					content = result.getContent();
					parserResult = parser.parse(result.getUrlAfterRedirect(), content);
				} else {
					throw new NoFeedFoundException(e);
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
		return urlProviders.stream()
				.flatMap(provider -> provider.get(url, urlContent).stream())
				.filter(StringUtils::isNotBlank)
				.findFirst()
				.orElse(null);
	}

	public record FeedFetcherResult(FeedParserResult feed, String urlAfterRedirect, String lastModifiedHeader, String lastETagHeader,
			String contentHash, Duration validFor) {
	}

	public static class NoFeedFoundException extends Exception {
		private static final long serialVersionUID = 1L;

		public NoFeedFoundException(Throwable cause) {
			super("This URL does not point to an RSS feed or a website with an RSS feed.", cause);
		}
	}

}
