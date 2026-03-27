package com.commafeed.integration.rest;

import java.time.Duration;
import java.time.Instant;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import com.commafeed.TestConstants;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.resource.CategoryREST;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
class LargeDatasetIT extends BaseIT {

	private static final int FEED_COUNT = 10;
	private static final int ENTRIES_PER_FEED = 20;
	private static final int TOTAL_ENTRIES = FEED_COUNT * ENTRIES_PER_FEED;

	@BeforeEach
	void setup() {
		initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
		RestAssured.authentication = RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);

		for (int i = 0; i < FEED_COUNT; i++) {
			String path = "/feed/" + i;
			getMockServerClient().when(HttpRequest.request().withMethod("GET").withPath(path))
					.respond(HttpResponse.response().withBody(generateFeed(i)).withContentType(MediaType.APPLICATION_XML));
			subscribe("http://localhost:" + getMockServerClient().getPort() + path);
		}

		Awaitility.await().atMost(Duration.ofSeconds(60)).until(() -> getAllEntries().getEntries().size(), count -> count >= TOTAL_ENTRIES);
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
	}

	@Test
	void markAllAsRead() {
		Entries before = getAllEntries();
		Assertions.assertEquals(TOTAL_ENTRIES, before.getEntries().size());
		Assertions.assertTrue(before.getEntries().stream().noneMatch(Entry::isRead));

		MarkRequest markRequest = new MarkRequest();
		markRequest.setId(CategoryREST.ALL);
		markRequest.setRead(true);
		RestAssured.given().body(markRequest).contentType(ContentType.JSON).post("rest/category/mark").then().statusCode(200);

		Entries after = getAllEntries();
		Assertions.assertEquals(TOTAL_ENTRIES, after.getEntries().size());
		Assertions.assertTrue(after.getEntries().stream().allMatch(Entry::isRead));
	}

	@Test
	void paginationHasMore() {
		Entries firstPage = RestAssured.given()
				.get("rest/category/entries?id=all&readType=all&limit=20&offset=0")
				.then()
				.statusCode(200)
				.extract()
				.as(Entries.class);
		Assertions.assertEquals(20, firstPage.getEntries().size());
		Assertions.assertTrue(firstPage.isHasMore());

		Entries lastPage = RestAssured.given()
				.get("rest/category/entries?id=all&readType=all&limit=20&offset={offset}", TOTAL_ENTRIES - 20)
				.then()
				.statusCode(200)
				.extract()
				.as(Entries.class);
		Assertions.assertEquals(20, lastPage.getEntries().size());
		Assertions.assertFalse(lastPage.isHasMore());
	}

	private Entries getAllEntries() {
		return RestAssured.given()
				.get("rest/category/entries?id=all&readType=all&limit=1000")
				.then()
				.statusCode(200)
				.extract()
				.as(Entries.class);
	}

	private String generateFeed(int feedIndex) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<rss version=\"2.0\">\n<channel>\n");
		sb.append("<title>Feed ").append(feedIndex).append("</title>\n");
		sb.append("<link>https://hostname.local/feed/").append(feedIndex).append("</link>\n");
		sb.append("<description>Test feed ").append(feedIndex).append("</description>\n");
		Instant base = Instant.parse("2024-01-01T00:00:00Z");
		for (int i = 0; i < ENTRIES_PER_FEED; i++) {
			sb.append("<item>\n");
			sb.append("<title>Feed ").append(feedIndex).append(" Item ").append(i).append("</title>\n");
			sb.append("<link>https://hostname.local/feed/").append(feedIndex).append("/item/").append(i).append("</link>\n");
			sb.append("<description>Description for feed ").append(feedIndex).append(" item ").append(i).append("</description>\n");
			sb.append("<pubDate>").append(base.minus(Duration.ofHours(i))).append("</pubDate>\n");
			sb.append("</item>\n");
		}
		sb.append("</channel>\n</rss>");
		return sb.toString();
	}
}
