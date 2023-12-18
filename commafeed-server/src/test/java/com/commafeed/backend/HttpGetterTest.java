package com.commafeed.backend;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpResponseException;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.CommaFeedConfiguration.ApplicationSettings;
import com.commafeed.backend.HttpGetter.HttpResult;
import com.commafeed.backend.HttpGetter.NotModifiedException;

@ExtendWith(MockServerExtension.class)
class HttpGetterTest {

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

		CommaFeedConfiguration config = new CommaFeedConfiguration();
		config.setApplicationSettings(settings);

		this.getter = new HttpGetter(config);
	}

	@ParameterizedTest
	@ValueSource(
			ints = { HttpStatus.UNAUTHORIZED_401, HttpStatus.FORBIDDEN_403, HttpStatus.NOT_FOUND_404,
					HttpStatus.INTERNAL_SERVER_ERROR_500 })
	void errorCodes(int code) {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(HttpResponse.response().withStatusCode(code));

		HttpResponseException e = Assertions.assertThrows(HttpResponseException.class, () -> getter.getBinary(this.feedUrl, 1000));
		Assertions.assertEquals(code, e.getStatusCode());
	}

	@Test
	void validFeed() throws IOException, NotModifiedException {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withBody(feedContent)
						.withContentType(MediaType.APPLICATION_ATOM_XML)
						.withHeader(HttpHeaders.LAST_MODIFIED, "123456")
						.withHeader(HttpHeaders.ETAG, "78910"));

		HttpResult result = getter.getBinary(this.feedUrl, 1000);
		Assertions.assertArrayEquals(feedContent, result.getContent());
		Assertions.assertEquals(MediaType.APPLICATION_ATOM_XML.toString(), result.getContentType());
		Assertions.assertEquals("123456", result.getLastModifiedSince());
		Assertions.assertEquals("78910", result.getETag());
		Assertions.assertTrue(result.getDuration() > 0);
		Assertions.assertEquals(this.feedUrl, result.getUrlAfterRedirect());
	}

	@Test
	void followRedirects() throws IOException, NotModifiedException {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withPath("/redirected"))
				.respond(HttpResponse.response().withBody(feedContent).withContentType(MediaType.APPLICATION_ATOM_XML));
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withStatusCode(HttpStatus.MOVED_PERMANENTLY_301)
						.withHeader(HttpHeaders.LOCATION, "http://localhost:" + this.mockServerClient.getPort() + "/redirected"));

		HttpResult result = getter.getBinary(this.feedUrl, 1000);
		Assertions.assertEquals("http://localhost:" + this.mockServerClient.getPort() + "/redirected", result.getUrlAfterRedirect());
	}

	@Test
	void timeout() {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response().withDelay(Delay.milliseconds(2000)));

		Assertions.assertThrows(SocketTimeoutException.class, () -> getter.getBinary(this.feedUrl, 1000));
	}

	@Test
	void userAgent() throws IOException, NotModifiedException {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withHeader(HttpHeaders.USER_AGENT, "http-getter-test"))
				.respond(HttpResponse.response().withBody("ok"));

		HttpResult result = getter.getBinary(this.feedUrl, 1000);
		Assertions.assertEquals("ok", new String(result.getContent()));
	}

	@Test
	void ignoreInvalidSsl() throws IOException, NotModifiedException {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET")).respond(HttpResponse.response().withBody("ok"));

		HttpResult result = getter.getBinary("https://localhost:" + this.mockServerClient.getPort(), 1000);
		Assertions.assertEquals("ok", new String(result.getContent()));
	}

	@Test
	void lastModifiedReturns304() throws IOException, NotModifiedException {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withHeader(HttpHeaders.IF_MODIFIED_SINCE, "123456"))
				.respond(HttpResponse.response().withStatusCode(HttpStatus.NOT_MODIFIED_304));

		Assertions.assertThrows(NotModifiedException.class, () -> getter.getBinary(this.feedUrl, "123456", null, 1000));
	}

	@Test
	void eTagReturns304() throws IOException, NotModifiedException {
		this.mockServerClient.when(HttpRequest.request().withMethod("GET").withHeader(HttpHeaders.IF_NONE_MATCH, "78910"))
				.respond(HttpResponse.response().withStatusCode(HttpStatus.NOT_MODIFIED_304));

		Assertions.assertThrows(NotModifiedException.class, () -> getter.getBinary(this.feedUrl, null, "78910", 1000));
	}

}