package com.commafeed.integration;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.commafeed.CommaFeedDropwizardAppExtension;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.request.LoginRequest;
import com.commafeed.frontend.model.request.SubscribeRequest;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockServerExtension.class)
public abstract class BaseIT {

	private static final HttpRequest FEED_REQUEST = HttpRequest.request().withMethod("GET").withPath("/");

	private final CommaFeedDropwizardAppExtension extension = new CommaFeedDropwizardAppExtension() {
		@Override
		protected JerseyClientBuilder clientBuilder() {
			return configureClientBuilder(super.clientBuilder().register(MultiPartFeature.class));
		}
	};

	private Client client;

	private String feedUrl;

	private String baseUrl;

	private String apiBaseUrl;

	private String webSocketUrl;

	private MockServerClient mockServerClient;

	protected JerseyClientBuilder configureClientBuilder(JerseyClientBuilder base) {
		return base;
	}

	@BeforeEach
	void init(MockServerClient mockServerClient) throws IOException {
		this.mockServerClient = mockServerClient;

		URL resource = Objects.requireNonNull(getClass().getResource("/feed/rss.xml"));
		mockServerClient.when(FEED_REQUEST).respond(HttpResponse.response().withBody(IOUtils.toString(resource, StandardCharsets.UTF_8)));

		this.client = extension.client();
		this.feedUrl = "http://localhost:" + mockServerClient.getPort() + "/";
		this.baseUrl = "http://localhost:" + extension.getLocalPort() + "/";
		this.apiBaseUrl = this.baseUrl + "rest/";
		this.webSocketUrl = "ws://localhost:" + extension.getLocalPort() + "/ws";
	}

	@AfterEach
	void cleanup() {
		this.client.close();
	}

	protected void feedNowReturnsMoreEntries() throws IOException {
		mockServerClient.clear(FEED_REQUEST);

		URL resource = Objects.requireNonNull(getClass().getResource("/feed/rss_2.xml"));
		mockServerClient.when(FEED_REQUEST).respond(HttpResponse.response().withBody(IOUtils.toString(resource, StandardCharsets.UTF_8)));
	}

	protected String login() {
		LoginRequest req = new LoginRequest();
		req.setName("admin");
		req.setPassword("admin");
		try (Response response = client.target(apiBaseUrl + "user/login").request().post(Entity.json(req))) {
			Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());
			return response.getCookies().get("JSESSIONID").getValue();
		}
	}

	protected Long subscribe(String feedUrl) {
		SubscribeRequest subscribeRequest = new SubscribeRequest();
		subscribeRequest.setUrl(feedUrl);
		subscribeRequest.setTitle("my title for this feed");
		return client.target(apiBaseUrl + "feed/subscribe").request().post(Entity.json(subscribeRequest), Long.class);
	}

	protected Long subscribeAndWaitForEntries(String feedUrl) {
		Long subscriptionId = subscribe(feedUrl);
		Awaitility.await().atMost(Duration.ofSeconds(15)).until(() -> getFeedEntries(subscriptionId), e -> e.getEntries().size() == 2);
		return subscriptionId;
	}

	protected Subscription getSubscription(Long subscriptionId) {
		return client.target(apiBaseUrl + "feed/get/{id}").resolveTemplate("id", subscriptionId).request().get(Subscription.class);
	}

	protected Entries getFeedEntries(long subscriptionId) {
		Response response = client.target(apiBaseUrl + "feed/entries")
				.queryParam("id", subscriptionId)
				.queryParam("readType", "all")
				.request()
				.get();
		return response.readEntity(Entries.class);
	}

	protected void forceRefreshAllFeeds() {
		client.target(apiBaseUrl + "feed/refreshAll").request().get(Void.class);
	}
}
