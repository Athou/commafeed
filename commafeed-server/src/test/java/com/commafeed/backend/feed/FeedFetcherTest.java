package com.commafeed.backend.feed;

import com.commafeed.backend.Digests;
import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.feed.FeedFetcher.FeedFetcherResult;
import com.commafeed.backend.feed.parser.FeedParser;
import com.commafeed.backend.feed.parser.FeedParser.FeedParsingException;
import com.commafeed.backend.feed.parser.FeedParserResult;
import com.commafeed.backend.urlprovider.FeedURLProvider;
import com.commafeed.backend.urlprovider.InPageReferenceFeedURLProvider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FeedFetcherTest {

    @Mock private FeedParser parser;

    @Mock private HttpGetter getter;

    @Mock private FeedURLProvider urlProvider;

    private FeedFetcher fetcher;

    @BeforeEach
    void init() {
        fetcher = new FeedFetcher(parser, getter, List.of(urlProvider));
    }

    @Test
    void findsUrlInPage() throws Exception {
        String htmlUrl = "https://aaa.com";
        byte[] html = "html".getBytes();
        Mockito.when(getter.get(HttpGetter.HttpRequest.builder(htmlUrl).build()))
                .thenReturn(new HttpResult(html, "text/html", null, null, htmlUrl, Duration.ZERO));
        Mockito.when(parser.parse(htmlUrl, html)).thenThrow(new FeedParsingException("invalid feed"));

        String feedUrl = "https://bbb.com/feed";
        byte[] feed = "feed".getBytes();
        Mockito.when(getter.get(HttpGetter.HttpRequest.builder(feedUrl).build()))
                .thenReturn(new HttpResult(feed, "application/atom+xml", null, null, feedUrl, Duration.ZERO));
        Mockito.when(parser.parse(feedUrl, feed))
                .thenReturn(new FeedParserResult("title", "link", "iconUrl", null, null, null, null));

        Mockito.when(urlProvider.get(htmlUrl, new String(html))).thenReturn(List.of(feedUrl));

        FeedFetcherResult result = fetcher.fetch(htmlUrl, true, null, null, null, null);
        Assertions.assertEquals("title", result.feed().title());
    }

    @ParameterizedTest
    @CsvSource({
        "https://example.com/blog/, feed.xml, https://example.com/blog/feed.xml",
        "https://other.example.com/blog/, feed.xml, https://other.example.com/blog/feed.xml",
        "https://other.example.com/blog/, /feed.xml, https://other.example.com/feed.xml",
        "https://example.com/blog/, https://feeds.example.com/rss, https://feeds.example.com/rss"
    })
    void findsUrlInRedirectedPage(String redirectedUrl, String href, String expectedFeedUrl) throws Exception {
        String originalUrl = "https://example.com/start";
        byte[] html =
                "<html><head><link type='application/rss+xml' href='%s'></head></html>"
                        .formatted(href)
                        .getBytes(StandardCharsets.UTF_8);
        Mockito.when(getter.get(HttpGetter.HttpRequest.builder(originalUrl).build()))
                .thenReturn(new HttpResult(html, "text/html", null, null, redirectedUrl, Duration.ZERO));
        Mockito.when(parser.parse(redirectedUrl, html)).thenThrow(new FeedParsingException("invalid feed"));

        byte[] feed = "feed".getBytes(StandardCharsets.UTF_8);
        Mockito.when(getter.get(HttpGetter.HttpRequest.builder(expectedFeedUrl).build()))
                .thenReturn(new HttpResult(feed, "application/rss+xml", null, null, expectedFeedUrl, Duration.ZERO));
        FeedParserResult parsedFeed = new FeedParserResult("title", "link", "iconUrl", null, null, null, null);
        Mockito.when(parser.parse(expectedFeedUrl, feed)).thenReturn(parsedFeed);

        FeedFetcher redirectedFetcher = new FeedFetcher(parser, getter, List.of(new InPageReferenceFeedURLProvider()));
        FeedFetcherResult result = redirectedFetcher.fetch(originalUrl, true, null, null, null, null);

        Assertions.assertEquals(parsedFeed, result.feed());
        Assertions.assertEquals(expectedFeedUrl, result.urlAfterRedirect());
        Mockito.verify(getter).get(HttpGetter.HttpRequest.builder(expectedFeedUrl).build());
    }

    @Test
    void updatesHeaderWhenContentDitNotChange() throws Exception {
        String url = "https://aaa.com";
        String lastModified = "last-modified-1";
        String etag = "etag-1";
        byte[] content = "content".getBytes();
        String lastContentHash = Digests.sha1Hex(content);

        Mockito.when(getter.get(HttpGetter.HttpRequest.builder(url).lastModified(lastModified).eTag(etag).build()))
                .thenReturn(new HttpResult(content, "content-type", "last-modified-2", "etag-2", null, Duration.ZERO));

        NotModifiedException e =
                Assertions.assertThrows(
                        NotModifiedException.class,
                        () -> fetcher.fetch(url, false, lastModified, etag, Instant.now(), lastContentHash));

        Assertions.assertEquals("last-modified-2", e.getNewLastModifiedHeader());
        Assertions.assertEquals("etag-2", e.getNewEtagHeader());
    }
}
