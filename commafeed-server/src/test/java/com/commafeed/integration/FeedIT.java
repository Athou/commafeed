package com.commafeed.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.commafeed.CommaFeedDropwizardAppExtension;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.request.SubscribeRequest;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockServerExtension.class)
class FeedIT {

	private static final CommaFeedDropwizardAppExtension EXT = new CommaFeedDropwizardAppExtension() {
		@Override
		protected JerseyClientBuilder clientBuilder() {
			HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("admin", "admin");
			return super.clientBuilder().register(feature);
		}
	};

	private MockServerClient mockServerClient;

	@BeforeEach
	void init(MockServerClient mockServerClient) throws IOException {
		this.mockServerClient = mockServerClient;
		this.mockServerClient.when(HttpRequest.request().withMethod("GET"))
				.respond(HttpResponse.response()
						.withBody(IOUtils.toString(getClass().getResource("/feed/rss.xml"), StandardCharsets.UTF_8)));
	}

	@Test
	void test() {
		Client client = EXT.client();

		String feedUrl = "http://localhost:" + this.mockServerClient.getPort();

		long subscriptionId = subscribe(client, feedUrl);
		Awaitility.await()
				.atMost(Duration.ofSeconds(15))
				.pollInterval(Duration.ofMillis(500))
				.until(() -> getFeedEntries(client, subscriptionId), e -> e.getEntries().size() == 2);
	}

	private Long subscribe(Client client, String feedUrl) {
		SubscribeRequest subscribeRequest = new SubscribeRequest();
		subscribeRequest.setUrl(feedUrl);
		subscribeRequest.setTitle("my title for this feed");
		Response response = client.target(String.format("http://localhost:%d/rest/feed/subscribe", EXT.getLocalPort()))
				.request()
				.post(Entity.json(subscribeRequest));
		Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());
		return response.readEntity(Long.class);
	}

	private Entries getFeedEntries(Client client, long subscriptionId) {
		Response response = client.target(String.format("http://localhost:%d/rest/feed/entries", EXT.getLocalPort()))
				.queryParam("id", subscriptionId)
				.queryParam("readType", "unread")
				.request()
				.get();
		return response.readEntity(Entries.class);
	}

}
