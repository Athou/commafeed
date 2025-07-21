package com.commafeed.integration.rest;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.backend.Digests;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.resource.fever.FeverResponse;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
class FeverIT extends BaseIT {

	private Long userId;
	private String apiKey;

	@BeforeEach
	void setup() {
		RestAssured.authentication = RestAssured.preemptive().basic("admin", "admin");

		// create api key
		ProfileModificationRequest req = new ProfileModificationRequest();
		req.setCurrentPassword("admin");
		req.setNewApiKey(true);
		RestAssured.given().body(req).contentType(ContentType.JSON).post("rest/user/profile").then().statusCode(HttpStatus.SC_OK);

		// retrieve api key
		UserModel user = RestAssured.given().get("rest/user/profile").then().statusCode(HttpStatus.SC_OK).extract().as(UserModel.class);
		this.apiKey = user.getApiKey();
		this.userId = user.getId();
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
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

	@Test
	void entries() {
		subscribeAndWaitForEntries(getFeedUrl());
		FeverResponse feverResponse = fetch("items");
		Assertions.assertEquals(2, feverResponse.getItems().size());
	}

	@Test
	void groups() {
		createCategory("category-1");
		FeverResponse feverResponse = fetch("groups");
		Assertions.assertEquals(1, feverResponse.getGroups().size());
		Assertions.assertEquals("category-1", feverResponse.getGroups().get(0).getTitle());
	}

	@Test
	void links() {
		FeverResponse feverResponse = fetch("links");
		Assertions.assertTrue(feverResponse.getLinks().isEmpty());
	}

	private FeverResponse fetch(String what) {
		return fetch(what, apiKey);
	}

	private FeverResponse fetch(String what, String apiKey) {
		return RestAssured.given()
				.auth()
				.none()
				.formParam("api_key", Digests.md5Hex("admin:" + apiKey))
				.formParam(what, 1)
				.post("rest/fever/user/{userId}", userId)
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract()
				.as(FeverResponse.class);
	}
}
