package com.commafeed.integration.rest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.resource.fever.FeverResponse;
import com.commafeed.integration.BaseIT;

class FeverIT extends BaseIT {

	private Long userId;
	private String apiKey;

	@BeforeEach
	void init() {
		// create api key
		ProfileModificationRequest req = new ProfileModificationRequest();
		req.setCurrentPassword("admin");
		req.setNewApiKey(true);
		try (Response response = getClient().target(getApiBaseUrl() + "user/profile").request().post(Entity.json(req))) {
			Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());
		}

		// retrieve api key
		UserModel user = getClient().target(getApiBaseUrl() + "user/profile").request().get(UserModel.class);
		this.apiKey = user.getApiKey();
		this.userId = user.getId();
	}

	@Test
	void get() {
		try (Response response = getClient().target(getApiBaseUrl() + "fever/user/${userId}")
				.resolveTemplate("userId", 1)
				.request()
				.get()) {
			Assertions.assertEquals("Welcome to the CommaFeed Fever API. Add this URL to your Fever-compatible reader.",
					response.readEntity(String.class));
		}
	}

	@Test
	void invalidApiKey() {
		FeverResponse response = fetch("feeds", "invalid-key");
		Assertions.assertFalse(response.isAuth());
	}

	@Test
	void validApiKey() {
		FeverResponse response = fetch("feeds", apiKey);
		Assertions.assertTrue(response.isAuth());
	}

	@Test
	void feeds() {
		subscribe(getFeedUrl());
		FeverResponse feverResponse = fetch("feeds");
		Assertions.assertEquals(1, feverResponse.getFeeds().size());
	}

	@Test
	void unreadEntries() {
		subscribeAndWaitForEntries(getFeedUrl());
		FeverResponse feverResponse = fetch("unread_item_ids");
		Assertions.assertEquals(2, feverResponse.getUnreadItemIds().size());
	}

	private FeverResponse fetch(String what) {
		return fetch(what, apiKey);
	}

	private FeverResponse fetch(String what, String apiKey) {
		Form form = new Form();
		form.param("api_key", DigestUtils.md5Hex("admin:" + apiKey));
		form.param(what, "1");
		try (Response response = getClient().target(getApiBaseUrl() + "fever/user/{userId}")
				.resolveTemplate("userId", userId)
				.request()
				.post(Entity.form(form))) {
			Assertions.assertEquals(HttpStatus.OK_200, response.getStatus());
			return response.readEntity(FeverResponse.class);
		}
	}
}
