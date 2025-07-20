package com.commafeed.integration.rest;

import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.Category;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.request.CategoryModificationRequest;
import com.commafeed.frontend.model.request.CollapseRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.model.request.StarRequest;
import com.commafeed.frontend.resource.CategoryREST;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class CategoryIT extends BaseIT {
	@BeforeEach
	void setup() {
		RestAssured.authentication = RestAssured.preemptive().basic("admin", "admin");
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
	}

	@Test
	void modifyCategory() {
		String category1Id = createCategory("test-category-1");
		String category2Id = createCategory("test-category-2");

		CategoryModificationRequest request = new CategoryModificationRequest();
		request.setId(Long.valueOf(category2Id));
		request.setName("modified-category");
		request.setParentId(category1Id);
		RestAssured.given().body(request).contentType(MediaType.APPLICATION_JSON).post("rest/category/modify").then().statusCode(200);

		Category root = getRootCategory();
		Assertions.assertEquals(1, root.getChildren().size());
		Assertions.assertEquals("test-category-1", root.getChildren().get(0).getName());
		Assertions.assertEquals(1, root.getChildren().get(0).getChildren().size());
		Assertions.assertEquals("modified-category", root.getChildren().get(0).getChildren().get(0).getName());
	}

	@Test
	void collapseCategory() {
		String categoryId = createCategory("test-category");

		Category root = getRootCategory();
		Assertions.assertEquals(1, root.getChildren().size());
		Assertions.assertTrue(root.getChildren().get(0).isExpanded());

		CollapseRequest request = new CollapseRequest();
		request.setId(Long.valueOf(categoryId));
		request.setCollapse(true);
		RestAssured.given().body(request).contentType(MediaType.APPLICATION_JSON).post("rest/category/collapse").then().statusCode(200);

		root = getRootCategory();
		Assertions.assertEquals(1, root.getChildren().size());
		Assertions.assertFalse(root.getChildren().get(0).isExpanded());
	}

	@Test
	void deleteCategory() {
		String categoryId = createCategory("test-category");
		Assertions.assertEquals(1, getRootCategory().getChildren().size());

		IDRequest request = new IDRequest();
		request.setId(Long.valueOf(categoryId));
		RestAssured.given().body(request).contentType(MediaType.APPLICATION_JSON).post("rest/category/delete").then().statusCode(200);
		Assertions.assertEquals(0, getRootCategory().getChildren().size());
	}

	@Nested
	class GetEntries {
		@Test
		void all() {
			subscribeAndWaitForEntries(getFeedUrl());
			Entries entries = getCategoryEntries(CategoryREST.ALL);
			Assertions.assertEquals(2, entries.getEntries().size());
		}

		@Test
		void starred() {
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
			Assertions.assertEquals(0, getCategoryEntries(CategoryREST.STARRED).getEntries().size());

			Entry entry = getFeedEntries(subscriptionId).getEntries().get(0);

			StarRequest starRequest = new StarRequest();
			starRequest.setId(entry.getId());
			starRequest.setFeedId(subscriptionId);
			starRequest.setStarred(true);
			RestAssured.given().body(starRequest).contentType(MediaType.APPLICATION_JSON).post("rest/entry/star");

			Entries starredEntries = getCategoryEntries(CategoryREST.STARRED);
			Assertions.assertEquals(1, starredEntries.getEntries().size());
			Assertions.assertEquals(entry.getId(), starredEntries.getEntries().get(0).getId());
		}

		@Test
		void specificCategory() {
			String categoryId = createCategory("test-category");
			subscribeAndWaitForEntries(getFeedUrl(), categoryId);
			Entries entries = getCategoryEntries(categoryId);
			Assertions.assertEquals(2, entries.getEntries().size());
		}
	}
}
