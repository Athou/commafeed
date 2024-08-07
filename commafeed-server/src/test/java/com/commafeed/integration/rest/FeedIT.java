package com.commafeed.integration.rest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.awaitility.Awaitility;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.FeedInfo;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.request.FeedInfoRequest;
import com.commafeed.frontend.model.request.FeedModificationRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@QuarkusTest
class FeedIT extends BaseIT {

	@Override
	protected JerseyClientBuilder configureClientBuilder(JerseyClientBuilder base) {
		return base.register(HttpAuthenticationFeature.basic("admin", "admin"));
	}

	@Nested
	class Fetch {
		@Test
		void fetchFeed() {
			FeedInfoRequest req = new FeedInfoRequest();
			req.setUrl(getFeedUrl());

			FeedInfo feedInfo = getClient().target(getApiBaseUrl() + "feed/fetch").request().post(Entity.json(req), FeedInfo.class);
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
			try (Response response = getClient().target(getApiBaseUrl() + "feed/subscribe")
					.queryParam("url", getFeedUrl())
					.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
					.request()
					.get()) {
				Assertions.assertEquals(HttpStatus.SC_TEMPORARY_REDIRECT, response.getStatus());
			}
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

			try (Response response = getClient().target(getApiBaseUrl() + "feed/unsubscribe").request().post(Entity.json(request))) {
				return response.getStatus();
			}
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
			getClient().target(getApiBaseUrl() + "feed/mark").request().post(Entity.json(request), Void.TYPE);
		}
	}

	@Nested
	class Refresh {
		@Test
		void refresh() {
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// mariadb/mysql timestamp precision is 1 second
			Instant threshold = Instant.now().minus(Duration.ofSeconds(1));

			IDRequest request = new IDRequest();
			request.setId(subscriptionId);
			getClient().target(getApiBaseUrl() + "feed/refresh").request().post(Entity.json(request), Void.TYPE);

			Awaitility.await()
					.atMost(Duration.ofSeconds(15))
					.until(() -> getSubscription(subscriptionId), f -> f.getLastRefresh().isAfter(threshold));
		}

		@Test
		void refreshAll() {
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// mariadb/mysql timestamp precision is 1 second
			Instant threshold = Instant.now().minus(Duration.ofSeconds(1));
			getClient().target(getApiBaseUrl() + "feed/refreshAll").request().get(Void.TYPE);

			Awaitility.await()
					.atMost(Duration.ofSeconds(15))
					.until(() -> getSubscription(subscriptionId), f -> f.getLastRefresh().isAfter(threshold));
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
			getClient().target(getApiBaseUrl() + "feed/modify").request().post(Entity.json(req), Void.TYPE);

			subscription = getSubscription(subscriptionId);
			Assertions.assertEquals("new name", subscription.getName());
		}
	}

	@Nested
	class Favicon {
		@Test
		void favicon() throws IOException {
			Long subscriptionId = subscribe(getFeedUrl());

			byte[] icon = getClient().target(getApiBaseUrl() + "feed/favicon/{id}")
					.resolveTemplate("id", subscriptionId)
					.request()
					.get(byte[].class);
			byte[] defaultFavicon = IOUtils.toByteArray(Objects.requireNonNull(getClass().getResource("/images/default_favicon.gif")));
			Assertions.assertArrayEquals(defaultFavicon, icon);
		}
	}

	@Nested
	class Opml {
		@Test
		void importExportOpml() throws IOException {
			importOpml();
			String opml = getClient().target(getApiBaseUrl() + "feed/export").request().get(String.class);
			Assertions.assertTrue(opml.contains("<title>admin subscriptions in CommaFeed</title>"));
		}

		void importOpml() throws IOException {
			InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream("/opml/opml_v2.0.xml"));
			try (MultiPart multiPart = new MultiPart()) {
				multiPart.bodyPart(new StreamDataBodyPart("file", stream));
				multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

				getClient().target(getApiBaseUrl() + "feed/import")
						.request()
						.post(Entity.entity(multiPart, multiPart.getMediaType()), Void.TYPE);
			}
		}
	}

}
