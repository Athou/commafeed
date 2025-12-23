package com.commafeed.backend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.ConnectionOptions;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedVersion;
import com.commafeed.backend.HttpGetter.HttpResponseException;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.HttpGetter.TooManyRequestsException;
import com.google.common.net.HttpHeaders;

import io.quarkus.runtime.configuration.MemorySize;

@ExtendWith(MockServerExtension.class)
class HttpGetterTest {

	private static final Instant NOW = Instant.now();

	private MockServerClient mockServerClient;
	private String feedUrl;
	private byte[] feedContent;

	private CommaFeedConfiguration config;

	private HttpGetter getter;

	@BeforeEach
	void init(MockServerClient mockServerClient) throws IOException {
		this.mockServerClient = mockServerClient;
		this.mockServerClient.reset();
		this.feedUrl = "http://localhost:" + this.mockServerClient.getPort() + "/";
		this.feedContent = IOUtils.toByteArray(Objects.requireNonNull(getClass().getResource("/feed/rss.xml")));

		this.config = Mockito.mock(CommaFeedConfiguration.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.when(config.httpClient().userAgent()).thenReturn(Optional.of("http-getter-test"));
		Mockito.when(config.httpClient().connectTimeout()).thenReturn(Duration.ofSeconds(30));
		Mockito.when(config.httpClient().sslHandshakeTimeout()).thenReturn(Duration.ofSeconds(30));
		Mockito.when(config.httpClient().socketTimeout()).thenReturn(Duration.ofSeconds(30));
		Mockito.when(config.httpClient().responseTimeout()).thenReturn(Duration.ofSeconds(30));
		Mockito.when(config.httpClient().connectionTimeToLive()).thenReturn(Duration.ofSeconds(30));
		Mockito.when(config.httpClient().maxResponseSize()).thenReturn(new MemorySize(new BigInteger("10000")));
		Mockito.when(config.httpClient().cache().enabled()).thenReturn(true);
		Mockito.when(config.httpClient().cache().maximumMemorySize()).thenReturn(new MemorySize(new BigInteger("100000")));
		Mockito.when(config.httpClient().cache().expiration()).thenReturn(Duration.ofMinutes(1));
		Mockito.when(config.feedRefresh().httpThreads()).thenReturn(3);

		this.getter = new HttpGetter(config, () -> NOW, Mockito.mock(CommaFeedVersion.class), Mockito.mock(MetricRegistry.class));
	}

	@ParameterizedTest
	@ValueSource(
			ints = { HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_FORBIDDEN, HttpStatus.SC_NOT_FOUND, HttpStatus.SC_INTERNAL_SERVER_ERROR })
	void errorCodes(int code) {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(HttpResponse.response().withStatusCode(code));

		HttpResponseException e = Assertions.assertThrows(HttpResponseException.class, () -> getter.get(this.feedUrl));
		Assertions.assertEquals(code, e.getCode());
	}

	@Test
	void validFeed() throws Exception {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withBody(feedContent)
						.withContentType(MediaType.APPLICATION_ATOM_XML)
						.withHeader(HttpHeaders.LAST_MODIFIED, "123456")
						.withHeader(HttpHeaders.ETAG, "78910")
						.withHeader(HttpHeaders.CACHE_CONTROL, "max-age=60, must-revalidate")
						.withHeader(HttpHeaders.RETRY_AFTER, "120"));

		HttpResult result = getter.get(this.feedUrl);
		Assertions.assertArrayEquals(feedContent, result.content());
		Assertions.assertEquals(MediaType.APPLICATION_ATOM_XML.toString(), result.contentType());
		Assertions.assertEquals("123456", result.lastModifiedSince());
		Assertions.assertEquals("78910", result.eTag());
		Assertions.assertEquals(Duration.ofSeconds(60), result.validFor());
		Assertions.assertEquals(this.feedUrl, result.urlAfterRedirect());
	}

	@Test
	void ignoreInvalidCacheControlValue() throws Exception {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withBody(feedContent)
						.withContentType(MediaType.APPLICATION_ATOM_XML)
						.withHeader(HttpHeaders.CACHE_CONTROL, "max-age=60; must-revalidate"));

		HttpResult result = getter.get(this.feedUrl);
		Assertions.assertEquals(Duration.ZERO, result.validFor());
	}

	@Test
	void tooManyRequestsExceptionSeconds() {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(
						HttpResponse.response().withStatusCode(HttpStatus.SC_TOO_MANY_REQUESTS).withHeader(HttpHeaders.RETRY_AFTER, "120"));

		TooManyRequestsException e = Assertions.assertThrows(TooManyRequestsException.class, () -> getter.get(this.feedUrl));
		Assertions.assertEquals(NOW.plusSeconds(120), e.getRetryAfter());
	}

	@Test
	void tooManyRequestsExceptionDate() {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withStatusCode(HttpStatus.SC_TOO_MANY_REQUESTS)
						.withHeader(HttpHeaders.RETRY_AFTER, "Wed, 21 Oct 2015 07:28:00 GMT"));

		TooManyRequestsException e = Assertions.assertThrows(TooManyRequestsException.class, () -> getter.get(this.feedUrl));
		Assertions.assertEquals(Instant.parse("2015-10-21T07:28:00Z"), e.getRetryAfter());
	}

	@ParameterizedTest
	@ValueSource(
			ints = { HttpStatus.SC_MOVED_PERMANENTLY, HttpStatus.SC_MOVED_TEMPORARILY, HttpStatus.SC_TEMPORARY_REDIRECT,
					HttpStatus.SC_PERMANENT_REDIRECT })
	void followRedirects(int code) throws Exception {
		// first redirect
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withPath("/"))
				.respond(HttpResponse.response()
						.withStatusCode(code)
						.withHeader(HttpHeaders.LOCATION, "http://localhost:" + this.mockServerClient.getPort() + "/redirected"));

		// second redirect
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withPath("/redirected"))
				.respond(HttpResponse.response()
						.withStatusCode(code)
						.withHeader(HttpHeaders.LOCATION, "http://localhost:" + this.mockServerClient.getPort() + "/redirected-2"));

		// final destination
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withPath("/redirected-2"))
				.respond(HttpResponse.response().withBody(feedContent).withContentType(MediaType.APPLICATION_ATOM_XML));

		HttpResult result = getter.get(this.feedUrl);
		Assertions.assertEquals("http://localhost:" + this.mockServerClient.getPort() + "/redirected-2", result.urlAfterRedirect());
	}

	@Test
	void dataTimeout() {
		Mockito.when(config.httpClient().responseTimeout()).thenReturn(Duration.ofMillis(500));
		this.getter = new HttpGetter(config, () -> NOW, Mockito.mock(CommaFeedVersion.class), Mockito.mock(MetricRegistry.class));

		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response().withDelay(Delay.milliseconds(1000)));

		Assertions.assertThrows(SocketTimeoutException.class, () -> getter.get(this.feedUrl));
	}

	@Test
	void connectTimeout() {
		Mockito.when(config.httpClient().connectTimeout()).thenReturn(Duration.ofMillis(500));
		this.getter = new HttpGetter(config, () -> NOW, Mockito.mock(CommaFeedVersion.class), Mockito.mock(MetricRegistry.class));
		// try to connect to a non-routable address
		// https://stackoverflow.com/a/904609
		Exception e = Assertions.assertThrows(Exception.class, () -> getter.get("http://10.255.255.1"));
		Assertions.assertTrue(e instanceof ConnectTimeoutException
				// A NoRouteToHostException can also be thrown in some cases
				// depending on the underlying network configuration
				// https://github.com/Athou/commafeed/issues/1876
				|| e instanceof NoRouteToHostException,
				"Expected ConnectTimeoutException or NoRouteToHostException, but got: " + e.getClass().getName());
	}

	@Test
	void userAgent() throws Exception {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withHeader(HttpHeaders.USER_AGENT, "http-getter-test"))
				.respond(HttpResponse.response().withBody("ok"));

		HttpResult result = getter.get(this.feedUrl);
		Assertions.assertEquals("ok", new String(result.content()));
	}

	@Test
	void lastModifiedReturns304() {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withHeader(HttpHeaders.IF_MODIFIED_SINCE, "123456"))
				.respond(HttpResponse.response().withStatusCode(HttpStatus.SC_NOT_MODIFIED));

		Assertions.assertThrows(NotModifiedException.class,
				() -> getter.get(HttpGetter.HttpRequest.builder(this.feedUrl).lastModified("123456").build()));
	}

	@Test
	void eTagReturns304() {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withHeader(HttpHeaders.IF_NONE_MATCH, "78910"))
				.respond(HttpResponse.response().withStatusCode(HttpStatus.SC_NOT_MODIFIED));

		Assertions.assertThrows(NotModifiedException.class,
				() -> getter.get(HttpGetter.HttpRequest.builder(this.feedUrl).eTag("78910").build()));
	}

	@Test
	void ignoreCookie() {
		AtomicInteger calls = new AtomicInteger();

		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(req -> {
			calls.incrementAndGet();

			if (req.containsHeader(HttpHeaders.COOKIE)) {
				throw new Exception("cookie should not be sent by the client");
			}

			return HttpResponse.response().withBody("ok").withHeader(HttpHeaders.SET_COOKIE, "foo=bar");
		});

		Assertions.assertDoesNotThrow(() -> getter.get(this.feedUrl));
		Assertions.assertDoesNotThrow(() -> getter.get(this.feedUrl + "?foo=bar"));
		Assertions.assertEquals(2, calls.get());
	}

	@Test
	void cacheSubsequentCalls() throws Exception {
		AtomicInteger calls = new AtomicInteger();

		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(req -> {
			calls.incrementAndGet();
			return HttpResponse.response().withBody("ok");
		});

		HttpResult result = getter.get(this.feedUrl);
		Assertions.assertEquals(result, getter.get(this.feedUrl));
		Assertions.assertEquals(1, calls.get());
	}

	@Test
	void largeFeedWithContentLengthHeader() {
		byte[] bytes = new byte[100000];
		Arrays.fill(bytes, (byte) 1);
		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(HttpResponse.response().withBody(bytes));

		IOException e = Assertions.assertThrows(IOException.class, () -> getter.get(this.feedUrl));
		Assertions.assertEquals("Response size (100000 bytes) exceeds the maximum allowed size (10000 bytes)", e.getMessage());
	}

	@Test
	void largeFeedWithoutContentLengthHeader() {
		byte[] bytes = new byte[100000];
		Arrays.fill(bytes, (byte) 1);
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withBody(bytes)
						.withConnectionOptions(ConnectionOptions.connectionOptions().withSuppressContentLengthHeader(true)));

		IOException e = Assertions.assertThrows(IOException.class, () -> getter.get(this.feedUrl));
		Assertions.assertEquals("Response size exceeds the maximum allowed size (10000 bytes)", e.getMessage());
	}

	@Test
	void ignoreInvalidSsl() throws Exception {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(HttpResponse.response().withBody("ok"));

		HttpResult result = getter.get("https://localhost:" + this.mockServerClient.getPort());
		Assertions.assertEquals("ok", new String(result.content()));
	}

	@Test
	void doesNotUseUpgradeProtocolHeader() {
		AtomicInteger calls = new AtomicInteger();

		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(req -> {
			calls.incrementAndGet();

			if (req.containsHeader(HttpHeaders.UPGRADE)) {
				throw new Exception("upgrade header should not be sent by the client");
			}

			return HttpResponse.response().withBody("ok");
		});

		Assertions.assertDoesNotThrow(() -> getter.get(this.feedUrl));
		Assertions.assertEquals(1, calls.get());
	}

	@Nested
	class Compression {

		private static final String ACCEPT_ENCODING = "gzip, deflate";

		@Test
		void gzip() throws Exception {
			supportsCompression("gzip", GZIPOutputStream::new);
		}

		@Test
		void deflate() throws Exception {
			supportsCompression("deflate", DeflaterOutputStream::new);
		}

		void supportsCompression(String encoding, CompressionOutputStreamFunction compressionOutputStreamFunction) throws Exception {
			String body = "my body";

			HttpGetterTest.this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(req -> {
				String acceptEncodingHeader = req.getFirstHeader(HttpHeaders.ACCEPT_ENCODING);
				if (!ACCEPT_ENCODING.equals(acceptEncodingHeader)) {
					throw new Exception("Wrong value in the Accept-Encoding header, should be '%s' but was '%s'".formatted(ACCEPT_ENCODING,
							acceptEncodingHeader));
				}

				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try (OutputStream compressionOutputStream = compressionOutputStreamFunction.apply(output)) {
					compressionOutputStream.write(body.getBytes());
				}

				return HttpResponse.response().withBody(output.toByteArray()).withHeader(HttpHeaders.CONTENT_ENCODING, encoding);
			});

			HttpResult result = getter.get(HttpGetterTest.this.feedUrl);
			Assertions.assertEquals(body, new String(result.content()));
		}

		@FunctionalInterface
		public interface CompressionOutputStreamFunction {
			OutputStream apply(OutputStream input) throws IOException;
		}

	}

	@Nested
	class SchemeNotAllowed {
		@Test
		void file() {
			Assertions.assertThrows(HttpGetter.SchemeNotAllowedException.class, () -> getter.get("file://localhost"));
		}

		@Test
		void ftp() {
			Assertions.assertThrows(HttpGetter.SchemeNotAllowedException.class, () -> getter.get("ftp://localhost"));
		}
	}

	@Nested
	class HostNotAllowed {

		@BeforeEach
		void init() {
			Mockito.when(config.httpClient().blockLocalAddresses()).thenReturn(true);
			getter = new HttpGetter(config, () -> NOW, Mockito.mock(CommaFeedVersion.class), Mockito.mock(MetricRegistry.class));
		}

		@Test
		void localhost() {
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://localhost"));
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://127.0.0.1"));
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://2130706433"));
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://0x7F.0x00.0x00.0X01"));
		}

		@Test
		void zero() {
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://0.0.0.0"));
		}

		@Test
		void linkLocal() {
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://169.254.12.34"));
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://169.254.169.254"));
		}

		@Test
		void multicast() {
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://224.2.3.4"));
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://239.255.255.254"));
		}

		@Test
		void privateIpv4Ranges() {
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://10.0.0.1"));
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://172.16.0.1"));
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://192.168.0.1"));
		}

		@Test
		void privateIpv6Ranges() {
			Assertions.assertThrows(HttpGetter.HostNotAllowedException.class, () -> getter.get("http://fd12:3456:789a:1::1"));
		}
	}

}