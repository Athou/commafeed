package com.commafeed.integration.rest;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.resource.fever.FeverResponse;
import com.commafeed.integration.BaseIT;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;

class FeverIT extends BaseIT {

	private Long userId;
	private String apiKey;

	@BeforeEach
	void init() {
		// create api key
		ProfileModificationRequest req = new ProfileModificationRequest();
		req.setCurrentPassword("admin");
		req.setNewApiKey(true);
		getClient().target(getApiBaseUrl() + "user/profile").request().post(Entity.json(req), Void.TYPE);

		// retrieve api key
		UserModel user = getClient().target(getApiBaseUrl() + "user/profile").request().get(UserModel.class);
		this.apiKey = user.getApiKey();
		this.userId = user.getId();
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
		return getClient().target(getApiBaseUrl() + "fever/user/{userId}")
				.resolveTemplate("userId", userId)
				.request()
				.post(Entity.form(form), FeverResponse.class);
	}
}
