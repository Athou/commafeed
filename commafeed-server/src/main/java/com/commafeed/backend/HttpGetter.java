package com.commafeed.backend;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.HttpClientCache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.CacheControl;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Lombok;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.entity.AbstractBinAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.jboss.resteasy.reactive.common.headers.CacheControlDelegate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Smart HTTP getter: handles gzip, ssl, last modified, and etag headers */
@Singleton
@Slf4j
public class HttpGetter {
    private final CommaFeedConfiguration config;
    private final InstantSource instantSource;
    private final CloseableHttpAsyncClient client;
    private final Cache<HttpRequest, HttpResponse> cache;

    public HttpGetter(
            CommaFeedConfiguration config,
            InstantSource instantSource,
            HttpClientFactory httpClientFactory,
            MetricRegistry metrics) {
        this.config = config;
        this.instantSource = instantSource;
        this.client = httpClientFactory.newClient(config.feedRefresh().httpThreads());
        this.cache = newCache(config);

        metrics.registerGauge(
                MetricRegistry.name(getClass(), "cache", "size"),
                () -> cache == null ? 0 : cache.size());
        metrics.registerGauge(
                MetricRegistry.name(getClass(), "cache", "memoryUsage"),
                () ->
                        cache == null
                                ? 0
                                : cache.asMap().values().stream()
                                        .mapToInt(e -> ArrayUtils.getLength(e.content))
                                        .sum());
    }

    public HttpResult get(String url)
            throws IOException,
                    NotModifiedException,
                    TooManyRequestsException,
                    SchemeNotAllowedException {
        return get(HttpRequest.builder(url).build());
    }

    public HttpResult get(HttpRequest request)
            throws IOException,
                    NotModifiedException,
                    TooManyRequestsException,
                    SchemeNotAllowedException {
        URI uri = URI.create(request.getUrl());
        ensureHttpScheme(uri.getScheme());

        final HttpResponse response;
        if (cache == null) {
            response = invoke(request);
        } else {
            try {
                response = cache.get(request, () -> invoke(request));
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IOException ioe) {
                    throw ioe;
                } else {
                    throw Lombok.sneakyThrow(e);
                }
            }
        }

        int code = response.code();
        if (code == HttpStatus.SC_TOO_MANY_REQUESTS
                || code == HttpStatus.SC_SERVICE_UNAVAILABLE && response.retryAfter() != null) {
            throw new TooManyRequestsException(response.retryAfter());
        }

        if (code == HttpStatus.SC_NOT_MODIFIED) {
            throw new NotModifiedException("'304 - not modified' http code received");
        }

        if (code >= 300) {
            throw new HttpResponseException(code, "Server returned HTTP error code " + code);
        }

        String lastModifiedHeader = response.lastModifiedHeader();
        String eTagHeader = response.eTagHeader();

        Duration validFor =
                Optional.ofNullable(response.cacheControl())
                        .filter(cc -> cc.getMaxAge() >= 0)
                        .map(cc -> Duration.ofSeconds(cc.getMaxAge()))
                        .orElse(Duration.ZERO);

        return new HttpResult(
                response.content(),
                response.contentType(),
                lastModifiedHeader,
                eTagHeader,
                response.urlAfterRedirect(),
                validFor);
    }

    private void ensureHttpScheme(String scheme) throws SchemeNotAllowedException {
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new SchemeNotAllowedException(scheme);
        }
    }

    private HttpResponse invoke(HttpRequest request) throws IOException {
        log.debug("fetching {}", request.getUrl());

        HttpClientContext context = HttpClientContext.create();
        context.setRequestConfig(
                RequestConfig.custom()
                        // causes issues with some feeds
                        // see https://github.com/Athou/commafeed/issues/1572
                        // and https://issues.apache.org/jira/browse/HTTPCLIENT-2344
                        .setProtocolUpgradeEnabled(false)
                        .build());

        long maxResponseSize = config.httpClient().maxResponseSize().asLongValue();
        AsyncRequestProducer req = SimpleRequestProducer.create(request.toSimpleHttpRequest());
        AsyncResponseConsumer<Message<org.apache.hc.core5.http.HttpResponse, byte[]>> resp =
                new BasicResponseConsumer<>(
                        new LimitedContentLengthEntityConsumer(
                                new LimitedBytesEntityConsumer(maxResponseSize), maxResponseSize));
        FutureCallback<Message<org.apache.hc.core5.http.HttpResponse, byte[]>> callback =
                new FutureCallback<>() {
                    @Override
                    public void completed(
                            Message<org.apache.hc.core5.http.HttpResponse, byte[]> result) {}

                    @Override
                    public void failed(Exception ex) {}

                    @Override
                    public void cancelled() {}
                };
        Future<Message<org.apache.hc.core5.http.HttpResponse, byte[]>> future =
                client.execute(req, resp, context, callback);

        try {
            Message<org.apache.hc.core5.http.HttpResponse, byte[]> message =
                    future.get(config.httpClient().responseTimeout().toSeconds(), TimeUnit.SECONDS);

            byte[] content = message.getBody();
            int code = message.getHead().getCode();
            String lastModifiedHeader =
                    Optional.ofNullable(message.getHead().getFirstHeader(HttpHeaders.LAST_MODIFIED))
                            .map(NameValuePair::getValue)
                            .map(StringUtils::trimToNull)
                            .orElse(null);
            String eTagHeader =
                    Optional.ofNullable(message.getHead().getFirstHeader(HttpHeaders.ETAG))
                            .map(NameValuePair::getValue)
                            .map(StringUtils::trimToNull)
                            .orElse(null);

            CacheControl cacheControl =
                    Optional.ofNullable(message.getHead().getFirstHeader(HttpHeaders.CACHE_CONTROL))
                            .map(NameValuePair::getValue)
                            .map(StringUtils::trimToNull)
                            .map(HttpGetter::toCacheControl)
                            .orElse(null);

            Instant retryAfter =
                    Optional.ofNullable(message.getHead().getFirstHeader(HttpHeaders.RETRY_AFTER))
                            .map(NameValuePair::getValue)
                            .map(StringUtils::trimToNull)
                            .map(this::toInstant)
                            .orElse(null);

            String contentType =
                    Optional.ofNullable(message.getHead().getFirstHeader(HttpHeaders.CONTENT_TYPE))
                            .map(NameValuePair::getValue)
                            .map(StringUtils::trimToNull)
                            .orElse(null);
            String urlAfterRedirect =
                    Optional.ofNullable(context.getRedirectLocations())
                            .map(RedirectLocations::getAll)
                            .map(l -> Iterables.getLast(l, null))
                            .map(URI::toString)
                            .orElse(request.getUrl());

            return new HttpResponse(
                    code,
                    lastModifiedHeader,
                    eTagHeader,
                    cacheControl,
                    retryAfter,
                    content,
                    contentType,
                    urlAfterRedirect);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted while fetching " + request.getUrl(), e);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new IOException("timed out while fetching " + request.getUrl(), e);
        } catch (ExecutionException e) {
            future.cancel(true);
            throw Lombok.sneakyThrow(e.getCause());
        }
    }

    private static CacheControl toCacheControl(String headerValue) {
        try {
            return CacheControlDelegate.INSTANCE.fromString(headerValue);
        } catch (Exception e) {
            log.debug("Invalid Cache-Control header: {}", headerValue);
            return null;
        }
    }

    private Instant toInstant(String headerValue) {
        if (headerValue == null) {
            return null;
        }

        if (StringUtils.isNumeric(headerValue)) {
            return instantSource.instant().plusSeconds(Long.parseLong(headerValue));
        }

        return DateUtils.parseStandardDate(headerValue);
    }

    private static Cache<HttpRequest, HttpResponse> newCache(CommaFeedConfiguration config) {
        HttpClientCache cacheConfig = config.httpClient().cache();
        if (!cacheConfig.enabled()) {
            return null;
        }

        return CacheBuilder.newBuilder()
                .weigher((HttpRequest key, HttpResponse value) ->
                        value.content() != null ? value.content().length : 0)
                .maximumWeight(cacheConfig.maximumMemorySize().asLongValue())
                .expireAfterWrite(cacheConfig.expiration())
                .build();
    }

    private record LimitedContentLengthEntityConsumer(
            @Delegate AsyncEntityConsumer<byte[]> delegate, long maxBytes)
            implements AsyncEntityConsumer<byte[]> {

        @Override
        public void streamStart(EntityDetails entityDetails, FutureCallback<byte[]> resultCallback)
                throws HttpException, IOException {
            long contentLength = entityDetails.getContentLength();
            if (contentLength > maxBytes) {
                throw new IOException(
                        "Response size (%s bytes) exceeds the maximum allowed size (%s bytes)"
                                .formatted(contentLength, maxBytes));
            }
            delegate.streamStart(entityDetails, resultCallback);
        }
    }

    @RequiredArgsConstructor
    private static class LimitedBytesEntityConsumer extends AbstractBinAsyncEntityConsumer<byte[]> {

        private final long maxBytes;
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        protected void streamStart(ContentType contentType) {}

        @Override
        protected int capacityIncrement() {
            return Integer.MAX_VALUE;
        }

        @Override
        protected void data(ByteBuffer src, boolean endOfStream) throws IOException {
            if (out.size() + src.remaining() > maxBytes) {
                throw new IOException(
                        "Response size exceeds the maximum allowed size (%s bytes)"
                                .formatted(maxBytes));
            }
            byte[] chunk = new byte[src.remaining()];
            src.get(chunk);
            out.write(chunk, 0, chunk.length);
        }

        @Override
        protected byte[] generateContent() {
            return out.toByteArray();
        }

        @Override
        public void releaseResources() {
            out.reset();
        }
    }

    public static class SchemeNotAllowedException extends Exception {
        private static final long serialVersionUID = 1L;

        public SchemeNotAllowedException(String scheme) {
            super("Scheme not allowed: " + scheme);
        }
    }

    @Getter
    public static class NotModifiedException extends Exception {
        private static final long serialVersionUID = 1L;

        /** if the value of this header changed, this is its new value */
        private final String newLastModifiedHeader;

        /** if the value of this header changed, this is its new value */
        private final String newEtagHeader;

        public NotModifiedException(String message) {
            this(message, null, null);
        }

        public NotModifiedException(
                String message, String newLastModifiedHeader, String newEtagHeader) {
            super(message);
            this.newLastModifiedHeader = newLastModifiedHeader;
            this.newEtagHeader = newEtagHeader;
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class TooManyRequestsException extends Exception {
        private static final long serialVersionUID = 1L;

        private final Instant retryAfter;
    }

    @Getter
    public static class HttpResponseException extends IOException {
        private static final long serialVersionUID = 1L;

        private final int code;

        public HttpResponseException(int code, String message) {
            super(message);
            this.code = code;
        }
    }

    @Builder(builderMethodName = "")
    @EqualsAndHashCode
    @Getter
    public static class HttpRequest {
        private String url;
        private String lastModified;
        private String eTag;

        public static HttpRequestBuilder builder(String url) {
            return new HttpRequestBuilder().url(url);
        }

        public SimpleHttpRequest toSimpleHttpRequest() {
            SimpleRequestBuilder builder = SimpleRequestBuilder.get(url);
            if (lastModified != null) {
                builder.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
            }
            if (eTag != null) {
                builder.addHeader(HttpHeaders.IF_NONE_MATCH, eTag);
            }
            return builder.build();
        }
    }

    private record HttpResponse(
            int code,
            String lastModifiedHeader,
            String eTagHeader,
            CacheControl cacheControl,
            Instant retryAfter,
            byte[] content,
            String contentType,
            String urlAfterRedirect) {}

    public record HttpResult(
            byte[] content,
            String contentType,
            String lastModifiedSince,
            String eTag,
            String urlAfterRedirect,
            Duration validFor) {}
}
