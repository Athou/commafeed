package com.commafeed.backend.service;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.Feed;

@ExtendWith(MockServerExtension.class)
class PubSubServiceTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private CommaFeedConfiguration config;

	@Mock
	private FeedService feedService;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private UnitOfWork unitOfWork;

	@Mock
	private Feed feed;

	private MockServerClient client;
	private PubSubService underTest;

	@BeforeEach
	public void init(MockServerClient client) {
		MockitoAnnotations.openMocks(this);

		this.client = client;
		this.client.reset();

		this.underTest = new PubSubService(config, feedService, unitOfWork);

		Integer port = client.getPort();
		String hubUrl = String.format("http://localhost:%s/hub", port);

		// setup feed
		feed = Mockito.mock(Feed.class);
		Mockito.when(feed.getPushHub()).thenReturn(hubUrl);
		Mockito.when(feed.getPushTopic()).thenReturn("foo");

		// setup config
		Mockito.when(config.getApplicationSettings().getPublicUrl()).thenReturn(hubUrl);
	}

	@Test
	void subscribe200() {
		// Arrange
		client.when(HttpRequest.request().withMethod("POST")).respond(HttpResponse.response().withStatusCode(200));

		// Act
		underTest.subscribe(feed);

		// Assert
		client.verify(HttpRequest.request()
				.withContentType(MediaType.APPLICATION_FORM_URLENCODED)
				.withHeader(HttpHeaders.USER_AGENT, "CommaFeed")
				.withMethod("POST")
				.withPath("/hub"));
		Mockito.verify(feed, Mockito.never()).setPushTopic(Mockito.anyString());
		Mockito.verifyNoInteractions(unitOfWork);
	}

	@Test
	void subscribe400WithPushpressError() {
		// Arrange
		client.when(HttpRequest.request().withMethod("POST"))
				.respond(HttpResponse.response().withStatusCode(400).withBody(" is value is not allowed.  You may only subscribe to"));

		// Act
		underTest.subscribe(feed);

		// Assert
		Mockito.verify(feed).setPushTopic(Mockito.anyString());
		Mockito.verify(unitOfWork).run(Mockito.any());
	}

	@Test
	void subscribe400WithoutPushpressError() {
		// Arrange
		client.when(HttpRequest.request().withMethod("POST")).respond(HttpResponse.response().withStatusCode(400));

		// Act
		underTest.subscribe(feed);

		// Assert
		Mockito.verify(feed, Mockito.never()).setPushTopic(Mockito.anyString());
		Mockito.verifyNoInteractions(unitOfWork);
	}

}