package com.commafeed.integration.rest;

import java.io.StringReader;
import java.util.List;

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import com.commafeed.frontend.model.Category;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.UnreadCount;
import com.commafeed.frontend.model.request.CategoryModificationRequest;
import com.commafeed.frontend.model.request.CollapseRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.StarRequest;
import com.commafeed.frontend.model.request.TagRequest;
import com.commafeed.frontend.resource.CategoryREST;
import com.commafeed.integration.BaseIT;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

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
		String category3Id = createCategory("test-category-3");

		CategoryModificationRequest request = new CategoryModificationRequest();
		request.setId(Long.valueOf(category2Id));
		request.setName("modified-category-2");
		request.setParentId(category1Id);
		request.setPosition(2);
		RestAssured.given().body(request).contentType(ContentType.JSON).post("rest/category/modify").then().statusCode(200);

		Category root = getRootCategory();
		Assertions.assertEquals(2, root.getChildren().size());
		Assertions.assertEquals("test-category-1", root.getChildren().get(0).getName());
		Assertions.assertEquals(1, root.getChildren().get(0).getChildren().size());
		Assertions.assertEquals("modified-category-2", root.getChildren().get(0).getChildren().get(0).getName());

		request = new CategoryModificationRequest();
		request.setId(Long.valueOf(category3Id));
		request.setPosition(0);
		RestAssured.given().body(request).contentType(ContentType.JSON).post("rest/category/modify").then().statusCode(200);

		root = getRootCategory();
		Assertions.assertEquals(2, root.getChildren().size());
		Assertions.assertEquals("test-category-3", root.getChildren().get(0).getName());
		Assertions.assertEquals("test-category-1", root.getChildren().get(1).getName());
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
		RestAssured.given().body(request).contentType(ContentType.JSON).post("rest/category/collapse").then().statusCode(200);

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
		RestAssured.given().body(request).contentType(ContentType.JSON).post("rest/category/delete").then().statusCode(200);
		Assertions.assertEquals(0, getRootCategory().getChildren().size());
	}

	@Test
	void unreadCount() {
		String categoryId = createCategory("test-category");
		Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl(), categoryId);
		Assertions.assertEquals(2, getCategoryEntries(categoryId).getEntries().size());

		UnreadCount[] counts = RestAssured.given()
				.get("rest/category/unreadCount")
				.then()
				.statusCode(200)
				.extract()
				.as(UnreadCount[].class);

		Assertions.assertEquals(1, counts.length);
		Assertions.assertEquals(subscriptionId, counts[0].getFeedId());
		Assertions.assertEquals(2, counts[0].getUnreadCount());
	}

	@Nested
	class MarkEntriesAsRead {
		@Test
		void all() {
			subscribeAndWaitForEntries(getFeedUrl());
			Assertions.assertTrue(getCategoryEntries(CategoryREST.ALL).getEntries().stream().noneMatch(Entry::isRead));

			MarkRequest request = new MarkRequest();
			request.setId(CategoryREST.ALL);
			request.setRead(true);
			RestAssured.given().body(request).contentType(ContentType.JSON).post("rest/category/mark").then().statusCode(200);
			Assertions.assertTrue(getCategoryEntries(CategoryREST.ALL).getEntries().stream().allMatch(Entry::isRead));
		}

		@Test
		void specificCategory() {
			String categoryId = createCategory("test-category");
			subscribeAndWaitForEntries(getFeedUrl(), categoryId);
			Assertions.assertTrue(getCategoryEntries(categoryId).getEntries().stream().noneMatch(Entry::isRead));

			MarkRequest request = new MarkRequest();
			request.setId(categoryId);
			request.setRead(true);
			RestAssured.given().body(request).contentType(ContentType.JSON).post("rest/category/mark").then().statusCode(200);
			Assertions.assertTrue(getCategoryEntries(categoryId).getEntries().stream().allMatch(Entry::isRead));
		}
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
		void allAsFeed() throws FeedException {
			subscribeAndWaitForEntries(getFeedUrl());
			String xml = RestAssured.given()
					.get("rest/category/entriesAsFeed?id=all")
					.then()
					.statusCode(HttpStatus.SC_OK)
					.contentType(ContentType.XML)
					.extract()
					.asString();

			InputSource source = new InputSource(new StringReader(xml));
			SyndFeed feed = new SyndFeedInput().build(source);
			Assertions.assertEquals(2, feed.getEntries().size());
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
			RestAssured.given().body(starRequest).contentType(ContentType.JSON).post("rest/entry/star");

			Entries starredEntries = getCategoryEntries(CategoryREST.STARRED);
			Assertions.assertEquals(1, starredEntries.getEntries().size());
			Assertions.assertEquals(entry.getId(), starredEntries.getEntries().get(0).getId());
		}

		@Test
		void tagged() {
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
			Assertions.assertEquals(0, getTaggedEntries("my-tag").getEntries().size());

			Entry entry = getFeedEntries(subscriptionId).getEntries().get(0);

			TagRequest tagRequest = new TagRequest();
			tagRequest.setEntryId(Long.valueOf(entry.getId()));
			tagRequest.setTags(List.of("my-tag"));
			RestAssured.given().body(tagRequest).contentType(ContentType.JSON).post("rest/entry/tag");

			Entries taggedEntries = getTaggedEntries("my-tag");
			Assertions.assertEquals(1, taggedEntries.getEntries().size());
			Assertions.assertEquals(entry.getId(), taggedEntries.getEntries().get(0).getId());
		}

		@Test
		void keywords() {
			subscribeAndWaitForEntries(getFeedUrl());
			Assertions.assertEquals(1, getCategoryEntries(CategoryREST.ALL, "Item 2 description").getEntries().size());
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
