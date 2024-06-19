package com.commafeed.integration;

import java.util.Base64;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.model.request.SubscribeRequest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

class SecurityIT extends BaseIT {

	@Test
	void notLoggedIn() {
		try (Response response = getClient().target(getApiBaseUrl() + "user/profile").request().get()) {
			Assertions.assertEquals(HttpStatus.UNAUTHORIZED_401, response.getStatus());
		}
	}

	@Test
	void wrongPassword() {
		String auth = "Basic " + Base64.getEncoder().encodeToString("admin:wrong-password".getBytes());
		try (Response response = getClient().target(getApiBaseUrl() + "user/profile")
				.request()
				.header(HttpHeaders.AUTHORIZATION, auth)
				.get()) {
			Assertions.assertEquals(HttpStatus.UNAUTHORIZED_401, response.getStatus());
		}
	}

	@Test
	void missingRole() {
		String auth = "Basic " + Base64.getEncoder().encodeToString("demo:demo".getBytes());
		try (Response response = getClient().target(getApiBaseUrl() + "admin/settings")
				.request()
				.header(HttpHeaders.AUTHORIZATION, auth)
				.get()) {
			Assertions.assertEquals(HttpStatus.FORBIDDEN_403, response.getStatus());
		}
	}

	@Test
	void apiKey() {
		String auth = "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes());

		// create api key
		ProfileModificationRequest req = new ProfileModificationRequest();
		req.setCurrentPassword("admin");
		req.setNewApiKey(true);
		getClient().target(getApiBaseUrl() + "user/profile")
				.request()
				.header(HttpHeaders.AUTHORIZATION, auth)
				.post(Entity.json(req))
				.close();

		// fetch api key
		String apiKey = getClient().target(getApiBaseUrl() + "user/profile")
				.request()
				.header(HttpHeaders.AUTHORIZATION, auth)
				.get(UserModel.class)
				.getApiKey();

		// subscribe to a feed
		SubscribeRequest subscribeRequest = new SubscribeRequest();
		subscribeRequest.setUrl(getFeedUrl());
		subscribeRequest.setTitle("my title for this feed");
		long subscriptionId = getClient().target(getApiBaseUrl() + "feed/subscribe")
				.request()
				.header(HttpHeaders.AUTHORIZATION, auth)
				.post(Entity.json(subscribeRequest), Long.class);

		// get entries with api key
		Entries entries = getClient().target(getApiBaseUrl() + "feed/entries")
				.queryParam("id", subscriptionId)
				.queryParam("readType", "unread")
				.queryParam("apiKey", apiKey)
				.request()
				.get(Entries.class);
		Assertions.assertEquals("my title for this feed", entries.getName());
	}
}
