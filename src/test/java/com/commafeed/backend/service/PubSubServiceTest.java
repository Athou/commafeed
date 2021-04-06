package com.commafeed.backend.service;

import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.MediaType;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.feed.FeedQueues;
import com.commafeed.backend.model.Feed;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {22441})
public class PubSubServiceTest {

    private PubSubService underTest;

    private MockServerClient client;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    CommaFeedConfiguration config;

    @Mock
    FeedQueues queues;

    @Mock
    Feed feed;

    private AutoCloseable mocks;

    @BeforeEach
    public void setUp(MockServerClient client) {
        this.client = client;
        mocks = MockitoAnnotations.openMocks(this);

        underTest = new PubSubService(config, queues);

        // setup feed
        feed = mock(Feed.class);
        when(feed.getPushHub()).thenReturn("http://localhost:22441/hub");
        when(feed.getPushTopic()).thenReturn("foo");

        // setup config
        when(config.getApplicationSettings().getPublicUrl()).thenReturn("http://localhost:22441/hub");
    }

    @AfterEach
    public void tearDown() throws Exception {
        mocks.close();
        client.reset();
    }

    @Test
    public void subscribe_200() {
        // Arrange
        client
                .when(request().withMethod("POST"))
                .respond(response().withStatusCode(200));

        // Act
        underTest.subscribe(feed);

        // Assert
        client.verify(request()
                .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .withHeader(HttpHeaders.USER_AGENT, "CommaFeed")
                .withMethod("POST")
                .withPath("/hub"));
        verify(feed, never()).setPushTopic(anyString());
        verifyNoInteractions(queues);
    }

    @Test
    public void subscribe_400_withPushpressError() {
        // Arrange
        client
                .when(request().withMethod("POST"))
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
        client
                .when(request().withMethod("POST"))
                .respond(response().withStatusCode(400));

        // Act
        underTest.subscribe(feed);

        // Assert
        verify(feed, never()).setPushTopic(anyString());
        verifyNoInteractions(queues);
    }

}