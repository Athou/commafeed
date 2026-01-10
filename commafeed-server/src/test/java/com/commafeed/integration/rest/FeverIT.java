package com.commafeed.integration.rest;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.commafeed.TestConstants;
import com.commafeed.backend.Digests;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.model.request.StarRequest;
import com.commafeed.frontend.resource.fever.FeverResponse;
import com.commafeed.frontend.resource.fever.FeverResponse.FeverItem;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.Setter;

@QuarkusTest
class FeverIT extends BaseIT {

	private FeverClient client;

	@BeforeEach
	void setup() {
		initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
		RestAssured.authentication = RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);

		// create api key
		ProfileModificationRequest req = new ProfileModificationRequest();
		req.setCurrentPassword(TestConstants.ADMIN_PASSWORD);
		req.setNewApiKey(true);
		RestAssured.given().body(req).contentType(ContentType.JSON).post("rest/user/profile").then().statusCode(HttpStatus.SC_OK);

		// retrieve api key
		UserModel user = RestAssured.given().get("rest/user/profile").then().statusCode(HttpStatus.SC_OK).extract().as(UserModel.class);
		this.client = new FeverClient(user.getId(), user.getApiKey());
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
	}

	@Test
	void invalidApiKey() {
		client.apiKey = "invalid-key";

		FeverResponse response = client.execute("feeds");
		Assertions.assertFalse(response.isAuth());
	}

	@Test
	void validApiKey() {
		FeverResponse response = client.execute("feeds");
		Assertions.assertTrue(response.isAuth());
	}

	@Test
	void feeds() {
		subscribe(getFeedUrl());
		FeverResponse feverResponse = client.execute("feeds");
		Assertions.assertEquals(1, feverResponse.getFeeds().size());
	}

	@Test
	void unreadEntries() {
		subscribeAndWaitForEntries(getFeedUrl());
		FeverResponse feverResponse = client.execute("unread_item_ids");
		Assertions.assertEquals(2, feverResponse.getUnreadItemIds().size());
	}

	@Test
	void entries() {
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
		FeverResponse feverResponse = client.execute("items");
		Assertions.assertEquals(2, feverResponse.getItems().size());

		FeverItem item = feverResponse.getItems().getFirst();
		Assertions.assertEquals(subscriptionId, item.getFeedId());
		Assertions.assertEquals("Item 2", item.getTitle());
		Assertions.assertEquals("Item 2 description", item.getHtml());
		Assertions.assertEquals("https://hostname.local/commafeed/2", item.getUrl());
		Assertions.assertFalse(item.isSaved());
		Assertions.assertFalse(item.isRead());
	}

	@Test
	void entriesByIds() {
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
		Entry entry = getFeedEntries(subscriptionId).getEntries().getFirst();

		FeverResponse feverResponse = client.execute("items", new Param("with_ids", entry.getId()));
		Assertions.assertEquals(1, feverResponse.getItems().size());
	}

	@Test
	void savedEntries() {
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
		Entry entry = getFeedEntries(subscriptionId).getEntries().getFirst();

		StarRequest starRequest = new StarRequest();
		starRequest.setId(entry.getId());
		starRequest.setFeedId(subscriptionId);
		starRequest.setStarred(true);
		RestAssured.given().body(starRequest).contentType(ContentType.JSON).post("rest/entry/star");

		FeverResponse feverResponse = client.execute("saved_item_ids");
		Assertions.assertEquals(1, feverResponse.getSavedItemIds().size());
	}

	@Test
	void markEntry() {
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
		Entry entry = getFeedEntries(subscriptionId).getEntries().getFirst();

		client.execute("_", new Param("mark", "item"), new Param("id", entry.getId()), new Param("as", "read"));
		Assertions.assertEquals(1, getFeedEntries(subscriptionId).getEntries().stream().filter(Entry::isRead).count());

		client.execute("_", new Param("mark", "item"), new Param("id", entry.getId()), new Param("as", "unread"));
		Assertions.assertEquals(0, getFeedEntries(subscriptionId).getEntries().stream().filter(Entry::isRead).count());
	}

	@Test
	void markFeed() {
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

		client.execute("_", new Param("mark", "feed"), new Param("id", String.valueOf(subscriptionId)), new Param("as", "read"));

		Assertions.assertTrue(getFeedEntries(subscriptionId).getEntries().stream().allMatch(Entry::isRead));
	}

	@Test
	void markCategory() {
		String categoryId = createCategory("test-category");
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl(), categoryId);

		client.execute("_", new Param("mark", "group"), new Param("id", String.valueOf(categoryId)), new Param("as", "read"));

		Assertions.assertTrue(getFeedEntries(subscriptionId).getEntries().stream().allMatch(Entry::isRead));
	}

	@Test
	void tagEntry() {
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
		Entry entry = getFeedEntries(subscriptionId).getEntries().getFirst();

		client.execute("_", new Param("mark", "item"), new Param("id", entry.getId()), new Param("as", "saved"));
		Assertions.assertEquals(1, getFeedEntries(subscriptionId).getEntries().stream().filter(Entry::isStarred).count());

		client.execute("_", new Param("mark", "item"), new Param("id", entry.getId()), new Param("as", "unsaved"));
		Assertions.assertEquals(0, getFeedEntries(subscriptionId).getEntries().stream().filter(Entry::isStarred).count());
	}

	@Test
	void groups() {
		createCategory("category-1");
		FeverResponse feverResponse = client.execute("groups");
		Assertions.assertEquals(1, feverResponse.getGroups().size());
		Assertions.assertEquals("category-1", feverResponse.getGroups().getFirst().getTitle());
	}

	@Test
	void links() {
		FeverResponse feverResponse = client.execute("links");
		Assertions.assertTrue(feverResponse.getLinks().isEmpty());
	}

	private static class FeverClient {
		private final Long userId;

		@Setter
		private String apiKey;

		public FeverClient(Long userId, String apiKey) {
			this.userId = userId;
			this.apiKey = apiKey;
		}

		private FeverResponse execute(String action, Param... params) {
			RequestSpecification spec = RestAssured.given()
					.auth()
					.none()
					.formParam("api_key", Digests.md5Hex("admin:" + apiKey))
					.formParam(action, 1);

			for (Param param : params) {
				spec.formParam(param.name(), param.value());
			}

			return spec.post("rest/fever/user/{userId}", userId).then().statusCode(HttpStatus.SC_OK).extract().as(FeverResponse.class);

		}
	}

	private record Param(String name, String value) {
	}
}
