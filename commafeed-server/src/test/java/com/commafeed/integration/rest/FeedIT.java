package com.commafeed.integration.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.FeedInfo;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.request.FeedInfoRequest;
import com.commafeed.frontend.model.request.FeedModificationRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.integration.BaseIT;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
class FeedIT extends BaseIT {

	@BeforeEach
	void setup() {
		RestAssured.authentication = RestAssured.preemptive().basic("admin", "admin");
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
	}

	@Nested
	class Fetch {
		@Test
		void fetchFeed() {
			FeedInfoRequest req = new FeedInfoRequest();
			req.setUrl(getFeedUrl());

			FeedInfo feedInfo = RestAssured.given()
					.body(req)
					.contentType(ContentType.JSON)
					.post("rest/feed/fetch")
					.then()
					.statusCode(HttpStatus.SC_OK)
					.extract()
					.as(FeedInfo.class);
			Assertions.assertEquals("CommaFeed test feed", feedInfo.getTitle());
			Assertions.assertEquals(getFeedUrl(), feedInfo.getUrl());
		}
	}

	@Nested
	class Subscribe {
		@Test
		void subscribeAndReadEntries() {
			long subscriptionId = subscribe(getFeedUrl());
			Awaitility.await().atMost(Duration.ofSeconds(15)).until(() -> getFeedEntries(subscriptionId), e -> e.getEntries().size() == 2);
		}

		@Test
		void subscribeFromUrl() {
			RestAssured.given()
					.queryParam("url", getFeedUrl())
					.redirects()
					.follow(false)
					.get("rest/feed/subscribe")
					.then()
					.statusCode(HttpStatus.SC_TEMPORARY_REDIRECT);
		}

		@Test
		void unsubscribeFromUnknownFeed() {
			Assertions.assertEquals(HttpStatus.SC_NOT_FOUND, unsubsribe(1L));
		}

		@Test
		void unsubscribeFromKnownFeed() {
			long subscriptionId = subscribe(getFeedUrl());
			Assertions.assertEquals(HttpStatus.SC_OK, unsubsribe(subscriptionId));
		}

		private int unsubsribe(long subscriptionId) {
			IDRequest request = new IDRequest();
			request.setId(subscriptionId);

			return RestAssured.given()
					.body(request)
					.contentType(ContentType.JSON)
					.post("rest/feed/unsubscribe")
					.then()
					.extract()
					.statusCode();
		}
	}

	@Nested
	class Mark {
		@Test
		void markWithoutDates() {
			long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
			markFeedEntries(subscriptionId, null, null);
			Assertions.assertTrue(getFeedEntries(subscriptionId).getEntries().stream().allMatch(Entry::isRead));
		}

		@Test
		void markOlderThan() {
			long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
			markFeedEntries(subscriptionId, LocalDate.of(2023, 12, 28).atStartOfDay().toInstant(ZoneOffset.UTC), null);
			Assertions.assertEquals(1, getFeedEntries(subscriptionId).getEntries().stream().filter(Entry::isRead).count());
		}

		@Test
		void markInsertedBeforeBeforeSubscription() {
			// mariadb/mysql timestamp precision is 1 second
			Instant threshold = Instant.now().minus(Duration.ofSeconds(1));

			long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
			markFeedEntries(subscriptionId, null, threshold);
			Assertions.assertTrue(getFeedEntries(subscriptionId).getEntries().stream().noneMatch(Entry::isRead));
		}

		@Test
		void markInsertedBeforeAfterSubscription() {
			long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// mariadb/mysql timestamp precision is 1 second
			Instant threshold = Instant.now().plus(Duration.ofSeconds(1));

			markFeedEntries(subscriptionId, null, threshold);
			Assertions.assertTrue(getFeedEntries(subscriptionId).getEntries().stream().allMatch(Entry::isRead));
		}

		private void markFeedEntries(long subscriptionId, Instant olderThan, Instant insertedBefore) {
			MarkRequest request = new MarkRequest();
			request.setId(String.valueOf(subscriptionId));
			request.setOlderThan(olderThan == null ? null : olderThan.toEpochMilli());
			request.setInsertedBefore(insertedBefore == null ? null : insertedBefore.toEpochMilli());

			RestAssured.given().body(request).contentType(ContentType.JSON).post("rest/feed/mark").then().statusCode(HttpStatus.SC_OK);
		}
	}

	@Nested
	class Refresh {

		@Test
		void refreshAll() {
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// mariadb/mysql timestamp precision is 1 second
			Instant threshold = Instant.now().minus(Duration.ofSeconds(1));
			Assertions.assertEquals(HttpStatus.SC_OK, forceRefreshAllFeeds());

			Awaitility.await()
					.atMost(Duration.ofSeconds(15))
					.until(() -> getSubscription(subscriptionId), f -> f.getLastRefresh().isAfter(threshold));

			Assertions.assertEquals(HttpStatus.SC_TOO_MANY_REQUESTS, forceRefreshAllFeeds());
		}
	}

	@Nested
	class RSS {
		@Test
		void allAsFeed() throws FeedException {
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());
			String xml = RestAssured.given()
					.get("rest/feed/entriesAsFeed?id={id}", subscriptionId)
					.then()
					.statusCode(HttpStatus.SC_OK)
					.contentType(ContentType.XML)
					.extract()
					.asString();

			InputSource source = new InputSource(new StringReader(xml));
			SyndFeed feed = new SyndFeedInput().build(source);
			Assertions.assertEquals(2, feed.getEntries().size());
		}
	}

	@Nested
	class Modify {
		@Test
		void modify() {
			Long subscriptionId = subscribe(getFeedUrl());

			Subscription subscription = getSubscription(subscriptionId);

			FeedModificationRequest req = new FeedModificationRequest();
			req.setId(subscriptionId);
			req.setName("new name");
			req.setCategoryId(subscription.getCategoryId());
			req.setPosition(1);
			RestAssured.given().body(req).contentType(ContentType.JSON).post("rest/feed/modify").then().statusCode(HttpStatus.SC_OK);

			subscription = getSubscription(subscriptionId);
			Assertions.assertEquals("new name", subscription.getName());
		}
	}

	@Nested
	class Favicon {
		@Test
		void favicon() throws IOException {
			Long subscriptionId = subscribe(getFeedUrl());

			byte[] icon = RestAssured.given()
					.get("rest/feed/favicon/{id}", subscriptionId)
					.then()
					.statusCode(HttpStatus.SC_OK)
					.header(HttpHeaders.CACHE_CONTROL, "max-age=2592000")
					.extract()
					.response()
					.asByteArray();
			byte[] defaultFavicon = IOUtils.toByteArray(Objects.requireNonNull(getClass().getResource("/images/default_favicon.gif")));
			Assertions.assertArrayEquals(defaultFavicon, icon);
		}
	}

	@Nested
	class Opml {
		@Test
		void importExportOpml() {
			importOpml();
			String opml = RestAssured.given().get("rest/feed/export").then().statusCode(HttpStatus.SC_OK).extract().asString();
			Assertions.assertTrue(opml.contains("<title>admin subscriptions in CommaFeed</title>"));
		}

		void importOpml() {
			InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream("/opml/opml_v2.0.xml"));

			RestAssured.given()
					.multiPart("file", "opml_v2.0.xml", stream, MediaType.MULTIPART_FORM_DATA)
					.post("rest/feed/import")
					.then()
					.statusCode(HttpStatus.SC_OK);
		}
	}

}
