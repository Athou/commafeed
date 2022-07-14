package com.commafeed.backend.service;

import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.feed.FeedQueues;
import com.commafeed.backend.model.Feed;

@RunWith(MockitoJUnitRunner.class)
public class PubSubServiceTest {

	@Rule
	public final MockServerRule mockServerRule = new MockServerRule(this, 22441);

	private PubSubService underTest;
	private MockServerClient mockServerClient;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private CommaFeedConfiguration config;

	@Mock
	private FeedQueues queues;

	@Mock
	private Feed feed;

	@Before
	public void init() {
		underTest = new PubSubService(config, queues);

		// setup feed
		feed = Mockito.mock(Feed.class);
		Mockito.when(feed.getPushHub()).thenReturn("http://localhost:22441/hub");
		Mockito.when(feed.getPushTopic()).thenReturn("foo");

		// setup config
		Mockito.when(config.getApplicationSettings().getPublicUrl()).thenReturn("http://localhost:22441/hub");
	}

	@Test
	public void subscribe200() {
		// Arrange
		mockServerClient.when(HttpRequest.request().withMethod("POST")).respond(HttpResponse.response().withStatusCode(200));

		// Act
		underTest.subscribe(feed);

		// Assert
		mockServerClient.verify(HttpRequest.request()
				.withContentType(MediaType.APPLICATION_FORM_URLENCODED)
				.withHeader(HttpHeaders.USER_AGENT, "CommaFeed")
				.withMethod("POST")
				.withPath("/hub"));
		Mockito.verify(feed, Mockito.never()).setPushTopic(Mockito.anyString());
		Mockito.verifyNoInteractions(queues);
	}

	@Test
	public void subscribe400WithPushpressError() {
		// Arrange
		mockServerClient.when(HttpRequest.request().withMethod("POST"))
				.respond(HttpResponse.response().withStatusCode(400).withBody(" is value is not allowed.  You may only subscribe to"));

		// Act
		underTest.subscribe(feed);

		// Assert
		Mockito.verify(feed).setPushTopic(Mockito.anyString());
		Mockito.verify(queues).giveBack(feed);
	}

	@Test
	public void subscribe400WithoutPushpressError() {
		// Arrange
		mockServerClient.when(HttpRequest.request().withMethod("POST")).respond(HttpResponse.response().withStatusCode(400));

		// Act
		underTest.subscribe(feed);

		// Assert
		Mockito.verify(feed, Mockito.never()).setPushTopic(Mockito.anyString());
		Mockito.verifyNoInteractions(queues);
	}

}