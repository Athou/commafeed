package com.commafeed.backend;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import com.commafeed.CommaFeedConfiguration.ApplicationSettings;
import com.commafeed.backend.HttpGetter.HttpResponseException;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.google.common.net.HttpHeaders;

import io.dropwizard.util.DataSize;

@ExtendWith(MockServerExtension.class)
class HttpGetterTest {

	private static final int TIMEOUT = 10000;

	private MockServerClient mockServerClient;
	private String feedUrl;
	private byte[] feedContent;
	private HttpGetter getter;

	@BeforeEach
	void init(MockServerClient mockServerClient) throws IOException {
		this.mockServerClient = mockServerClient;
		this.mockServerClient.reset();
		this.feedUrl = "http://localhost:" + this.mockServerClient.getPort() + "/";
		this.feedContent = IOUtils.toByteArray(Objects.requireNonNull(getClass().getResource("/feed/rss.xml")));

		ApplicationSettings settings = new ApplicationSettings();
		settings.setUserAgent("http-getter-test");
		settings.setBackgroundThreads(3);
		settings.setMaxFeedResponseSize(DataSize.kilobytes(10));

		CommaFeedConfiguration config = new CommaFeedConfiguration();
		config.setApplicationSettings(settings);

		this.getter = new HttpGetter(config, Mockito.mock(MetricRegistry.class));
	}

	@ParameterizedTest
	@ValueSource(
			ints = { HttpStatus.UNAUTHORIZED_401, HttpStatus.FORBIDDEN_403, HttpStatus.NOT_FOUND_404,
					HttpStatus.INTERNAL_SERVER_ERROR_500 })
	void errorCodes(int code) {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(HttpResponse.response().withStatusCode(code));

		HttpResponseException e = Assertions.assertThrows(HttpResponseException.class, () -> getter.getBinary(this.feedUrl, TIMEOUT));
		Assertions.assertEquals(code, e.getCode());
	}

	@Test
	void validFeed() throws Exception {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withBody(feedContent)
						.withContentType(MediaType.APPLICATION_ATOM_XML)
						.withHeader(HttpHeaders.LAST_MODIFIED, "123456")
						.withHeader(HttpHeaders.ETAG, "78910"));

		HttpResult result = getter.getBinary(this.feedUrl, TIMEOUT);
		Assertions.assertArrayEquals(feedContent, result.getContent());
		Assertions.assertEquals(MediaType.APPLICATION_ATOM_XML.toString(), result.getContentType());
		Assertions.assertEquals("123456", result.getLastModifiedSince());
		Assertions.assertEquals("78910", result.getETag());
		Assertions.assertTrue(result.getDuration() > 0);
		Assertions.assertEquals(this.feedUrl, result.getUrlAfterRedirect());
	}

	@ParameterizedTest
	@ValueSource(
			ints = { HttpStatus.MOVED_PERMANENTLY_301, HttpStatus.MOVED_TEMPORARILY_302, HttpStatus.TEMPORARY_REDIRECT_307,
					HttpStatus.PERMANENT_REDIRECT_308 })
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

		HttpResult result = getter.getBinary(this.feedUrl, TIMEOUT);
		Assertions.assertEquals("http://localhost:" + this.mockServerClient.getPort() + "/redirected-2", result.getUrlAfterRedirect());
	}

	@Test
	void dataTimeout() {
		int smallTimeout = 500;
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response().withDelay(Delay.milliseconds(smallTimeout * 2)));

		Assertions.assertThrows(SocketTimeoutException.class, () -> getter.getBinary(this.feedUrl, smallTimeout));
	}

	@Test
	void connectTimeout() {
		// try to connect to a non-routable address
		// https://stackoverflow.com/a/904609
		Assertions.assertThrows(ConnectTimeoutException.class, () -> getter.getBinary("http://10.255.255.1", 2000));
	}

	@Test
	void userAgent() throws Exception {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withHeader(HttpHeaders.USER_AGENT, "http-getter-test"))
				.respond(HttpResponse.response().withBody("ok"));

		HttpResult result = getter.getBinary(this.feedUrl, TIMEOUT);
		Assertions.assertEquals("ok", new String(result.getContent()));
	}

	@Test
	void lastModifiedReturns304() {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withHeader(HttpHeaders.IF_MODIFIED_SINCE, "123456"))
				.respond(HttpResponse.response().withStatusCode(HttpStatus.NOT_MODIFIED_304));

		Assertions.assertThrows(NotModifiedException.class, () -> getter.getBinary(this.feedUrl, "123456", null, TIMEOUT));
	}

	@Test
	void eTagReturns304() {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withHeader(HttpHeaders.IF_NONE_MATCH, "78910"))
				.respond(HttpResponse.response().withStatusCode(HttpStatus.NOT_MODIFIED_304));

		Assertions.assertThrows(NotModifiedException.class, () -> getter.getBinary(this.feedUrl, null, "78910", TIMEOUT));
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

		Assertions.assertDoesNotThrow(() -> getter.getBinary(this.feedUrl, TIMEOUT));
		Assertions.assertDoesNotThrow(() -> getter.getBinary(this.feedUrl, TIMEOUT));
		Assertions.assertEquals(2, calls.get());
	}

	@Test
	void supportsCompression() {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(req -> {
			String acceptEncodingHeader = req.getFirstHeader(HttpHeaders.ACCEPT_ENCODING);
			if (!acceptEncodingHeader.contains("deflate")) {
				throw new Exception("deflate should be in the Accept-Encoding header");
			}
			if (!acceptEncodingHeader.contains("gzip")) {
				throw new Exception("gzip should be in the Accept-Encoding header");
			}

			return HttpResponse.response().withBody("ok");
		});

		Assertions.assertDoesNotThrow(() -> getter.getBinary(this.feedUrl, TIMEOUT));
	}

	@Test
	void largeFeedWithContentLengthHeader() {
		byte[] bytes = new byte[(int) DataSize.kilobytes(100).toBytes()];
		Arrays.fill(bytes, (byte) 1);
		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(HttpResponse.response().withBody(bytes));

		IOException e = Assertions.assertThrows(IOException.class, () -> getter.getBinary(this.feedUrl, TIMEOUT));
		Assertions.assertEquals("Response size (100000 bytes) exceeds the maximum allowed size (10000 bytes)", e.getMessage());
	}

	@Test
	void largeFeedWithoutContentLengthHeader() {
		byte[] bytes = new byte[(int) DataSize.kilobytes(100).toBytes()];
		Arrays.fill(bytes, (byte) 1);
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withBody(bytes)
						.withConnectionOptions(ConnectionOptions.connectionOptions().withSuppressContentLengthHeader(true)));

		IOException e = Assertions.assertThrows(IOException.class, () -> getter.getBinary(this.feedUrl, TIMEOUT));
		Assertions.assertEquals("Response size exceeds the maximum allowed size (10000 bytes)", e.getMessage());
	}

	@Test
	void ignoreInvalidSsl() throws Exception {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(HttpResponse.response().withBody("ok"));

		HttpResult result = getter.getBinary("https://localhost:" + this.mockServerClient.getPort(), TIMEOUT);
		Assertions.assertEquals("ok", new String(result.getContent()));
	}

}