package com.commafeed.integration.rest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.FeedInfo;
import com.commafeed.frontend.model.Subscription;
import com.commafeed.frontend.model.request.FeedInfoRequest;
import com.commafeed.frontend.model.request.FeedModificationRequest;
import com.commafeed.frontend.model.request.IDRequest;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.integration.BaseIT;

class FeedIT extends BaseIT {

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
				Assertions.assertEquals(HttpStatus.TEMPORARY_REDIRECT_307, response.getStatus());
			}
		}

		@Test
		void unsubscribeFromUnknownFeed() {
			Assertions.assertEquals(HttpStatus.NOT_FOUND_404, unsubsribe(1L));
		}

		@Test
		void unsubscribeFromKnownFeed() {
			long subscriptionId = subscribe(getFeedUrl());
			Assertions.assertEquals(HttpStatus.OK_200, unsubsribe(subscriptionId));
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
		void mark() {
			long subscriptionId = subscribe(getFeedUrl());
			Entries entries = Awaitility.await()
					.atMost(Duration.ofSeconds(15))
					.until(() -> getFeedEntries(subscriptionId), e -> e.getEntries().size() == 2);
			Assertions.assertTrue(entries.getEntries().stream().noneMatch(Entry::isRead));

			markFeedEntries(subscriptionId);
			Awaitility.await()
					.atMost(Duration.ofSeconds(15))
					.until(() -> getFeedEntries(subscriptionId), e -> e.getEntries().stream().allMatch(Entry::isRead));
		}

		private void markFeedEntries(long subscriptionId) {
			MarkRequest request = new MarkRequest();
			request.setId(String.valueOf(subscriptionId));
			getClient().target(getApiBaseUrl() + "feed/mark").request().post(Entity.json(request), Void.TYPE);
		}
	}

	@Nested
	class Refresh {
		@Test
		void refresh() {
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			Date now = new Date();
			IDRequest request = new IDRequest();
			request.setId(subscriptionId);
			getClient().target(getApiBaseUrl() + "feed/refresh").request().post(Entity.json(request), Void.TYPE);

			Awaitility.await()
					.atMost(Duration.ofSeconds(15))
					.until(() -> getSubscription(subscriptionId), f -> f.getLastRefresh().after(now));
		}

		@Test
		void refreshAll() {
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			Date now = new Date();
			getClient().target(getApiBaseUrl() + "feed/refreshAll").request().get(Void.TYPE);

			Awaitility.await()
					.atMost(Duration.ofSeconds(15))
					.until(() -> getSubscription(subscriptionId), f -> f.getLastRefresh().after(now));
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
		void importExportOpml() {
			importOpml();
			String opml = getClient().target(getApiBaseUrl() + "feed/export").request().get(String.class);
			String expextedOpml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<opml version=\"1.0\">\n" + "  <head>\n"
					+ "    <title>admin subscriptions in CommaFeed</title>\n" + "  </head>\n" + "  <body>\n"
					+ "    <outline text=\"out1\" title=\"out1\">\n"
					+ "      <outline text=\"feed1\" type=\"rss\" title=\"feed1\" xmlUrl=\"https://hostname.local/commafeed/feed1.xml\" />\n"
					+ "    </outline>\n" + "  </body>\n" + "</opml>\n";
			Assertions.assertEquals(StringUtils.normalizeSpace(expextedOpml), StringUtils.normalizeSpace(opml));
		}

		void importOpml() {
			InputStream stream = Objects.requireNonNull(getClass().getResourceAsStream("/opml/opml_v2.0.xml"));
			MultiPart multiPart = new MultiPart().bodyPart(new StreamDataBodyPart("file", stream));
			multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

			getClient().target(getApiBaseUrl() + "feed/import")
					.request()
					.post(Entity.entity(multiPart, multiPart.getMediaType()), Void.TYPE);
		}
	}

}
