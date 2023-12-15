package com.commafeed.integration;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
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
import lombok.Getter;

@Getter
@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockServerExtension.class)
abstract class BaseIT {

	private static final CommaFeedDropwizardAppExtension EXT = new CommaFeedDropwizardAppExtension() {
		@Override
		protected JerseyClientBuilder clientBuilder() {
			return super.clientBuilder().register(HttpAuthenticationFeature.basic("admin", "admin")).register(MultiPartFeature.class);
		}
	};

	private Client client;

	private String feedUrl;

	private String baseUrl;

	private String apiBaseUrl;

	private String webSocketUrl;

	@BeforeEach
	void init(MockServerClient mockServerClient) throws IOException {
		URL resource = Objects.requireNonNull(getClass().getResource("/feed/rss.xml"));
		mockServerClient.when(HttpRequest.request().withMethod("GET").withPath("/"))
				.respond(HttpResponse.response().withBody(IOUtils.toString(resource, StandardCharsets.UTF_8)));

		this.client = EXT.client();
		this.feedUrl = "http://localhost:" + mockServerClient.getPort() + "/";
		this.baseUrl = "http://localhost:" + EXT.getLocalPort() + "/";
		this.apiBaseUrl = this.baseUrl + "rest/";
		this.webSocketUrl = "ws://localhost:" + EXT.getLocalPort() + "/ws";
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
		try (Response response = client.target(apiBaseUrl + "feed/subscribe").request().post(Entity.json(subscribeRequest))) {
			Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());
			return response.readEntity(Long.class);
		}
	}

	protected Subscription getSubscription(Long subscriptionId) {
		try (Response response = client.target(apiBaseUrl + "feed/get")
				.path("{id}")
				.resolveTemplate("id", subscriptionId)
				.request()
				.get()) {
			Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());
			return response.readEntity(Subscription.class);
		}
	}

	protected Entries getFeedEntries(long subscriptionId) {
		Response response = client.target(apiBaseUrl + "feed/entries")
				.queryParam("id", subscriptionId)
				.queryParam("readType", "unread")
				.request()
				.get();
		return response.readEntity(Entries.class);
	}
}
