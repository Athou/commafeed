package com.commafeed.backend.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.MediaType;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.feed.FeedQueues;
import com.commafeed.backend.model.Feed;

@RunWith(MockitoJUnitRunner.class)
public class PubSubServiceTest {

	PubSubService underTest;

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 22441);
	public MockServerClient mockServerClient;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	CommaFeedConfiguration config;

	@Mock
	FeedQueues queues;

	@Mock
	Feed feed;

	@Before
	public void init() {
		underTest = new PubSubService(config, queues);

		// setup feed
		feed = mock(Feed.class);
		when(feed.getPushHub()).thenReturn("http://localhost:22441/hub");
		when(feed.getPushTopic()).thenReturn("foo");

		// setup config
		when(config.getApplicationSettings().getPublicUrl()).thenReturn("http://localhost:22441/hub");
	}

	@Test
	public void subscribe_200() {
		// Arrange
		mockServerClient.when(request().withMethod("POST")).respond(response().withStatusCode(200));

		// Act
		underTest.subscribe(feed);

		// Assert
		mockServerClient.verify(request().withContentType(MediaType.APPLICATION_FORM_URLENCODED)
				.withHeader(HttpHeaders.USER_AGENT, "CommaFeed")
				.withMethod("POST")
				.withPath("/hub"));
		verify(feed, never()).setPushTopic(anyString());
		verifyZeroInteractions(queues);
	}

	@Test
	public void subscribe_400_withPushpressError() {
		// Arrange
		mockServerClient.when(request().withMethod("POST"))
				.respond(response().withStatusCode(400).withBody(" is value is not allowed.  You may only subscribe to"));

		// Act
		underTest.subscribe(feed);

		// Assert
		verify(feed).setPushTopic(anyString());
		verify(queues).giveBack(feed);
	}

	@Test
	public void subscribe_400_withoutPushpressError() {
		// Arrange
		mockServerClient.when(request().withMethod("POST")).respond(response().withStatusCode(400));

		// Act
		underTest.subscribe(feed);

		// Assert
		verify(feed, never()).setPushTopic(anyString());
		verifyZeroInteractions(queues);
	}

}