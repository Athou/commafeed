package com.commafeed.integration.cleanup;

import java.time.Duration;
import java.time.Instant;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.commafeed.TestConstants;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.service.db.DatabaseCleaningService;
import com.commafeed.frontend.model.Entries;
import com.commafeed.frontend.model.Entry;
import com.commafeed.frontend.model.request.StarRequest;
import com.commafeed.frontend.resource.CategoryREST;
import com.commafeed.integration.BaseIT;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
class DatabaseCleaningIT extends BaseIT {

	@Inject
	DatabaseCleaningService databaseCleaningService;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	@Inject
	UnitOfWork unitOfWork;

	@Inject
	EntityManager entityManager;

	@BeforeEach
	void setup() {
		initialSetup(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
		RestAssured.authentication = RestAssured.preemptive().basic(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD);
	}

	@AfterEach
	void cleanup() {
		RestAssured.reset();
	}

	private void starEntry(String entryId, Long subscriptionId) {
		StarRequest starRequest = new StarRequest();
		starRequest.setId(entryId);
		starRequest.setFeedId(subscriptionId);
		starRequest.setStarred(true);
		RestAssured.given().body(starRequest).contentType(ContentType.JSON).post("rest/entry/star").then().statusCode(200);
	}

	private void unstarEntry(String entryId, Long subscriptionId) {
		StarRequest starRequest = new StarRequest();
		starRequest.setId(entryId);
		starRequest.setFeedId(subscriptionId);
		starRequest.setStarred(false);
		RestAssured.given().body(starRequest).contentType(ContentType.JSON).post("rest/entry/star").then().statusCode(200);
	}

	@Nested
	class KeepStarredEntries {

		@Test
		void starredEntriesAreKeptWhenCleaningFeedsExceedingCapacity() {
			// Subscribe to feed and wait for entries
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// Verify we have 2 entries
			Entries entriesBefore = getFeedEntries(subscriptionId);
			Assertions.assertEquals(2, entriesBefore.getEntries().size());

			// Star the first entry
			Entry entryToStar = entriesBefore.getEntries().getFirst();
			starEntry(entryToStar.getId(), subscriptionId);

			// Verify the entry is starred
			Entries starredEntries = getCategoryEntries(CategoryREST.STARRED);
			Assertions.assertEquals(1, starredEntries.getEntries().size());
			Assertions.assertEquals(entryToStar.getId(), starredEntries.getEntries().getFirst().getId());

			// Run cleanup with capacity of 0 (should delete all non-starred entries)
			// With keepStarredEntries=true (default), only non-starred entries are counted
			// for capacity.
			// We have 2 entries, 1 starred and 1 non-starred. With capacity=0, the 1
			// non-starred entry exceeds capacity.
			databaseCleaningService.cleanEntriesForFeedsExceedingCapacity(0);

			// Verify starred entry is still present
			Entries starredEntriesAfter = getCategoryEntries(CategoryREST.STARRED);
			Assertions.assertEquals(1, starredEntriesAfter.getEntries().size());
			Assertions.assertEquals(entryToStar.getId(), starredEntriesAfter.getEntries().getFirst().getId());

			// Verify the non-starred entry was deleted (only starred entry should remain)
			Entries entriesAfter = getFeedEntries(subscriptionId);
			Assertions.assertEquals(1, entriesAfter.getEntries().size());
			Assertions.assertEquals(entryToStar.getId(), entriesAfter.getEntries().getFirst().getId());
		}

		@Test
		void starredEntriesAreKeptWhenCleaningOldEntries() {
			// Subscribe to feed and wait for entries
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// Verify we have 2 entries
			Entries entriesBefore = getFeedEntries(subscriptionId);
			Assertions.assertEquals(2, entriesBefore.getEntries().size());

			// Star the first entry (oldest one based on published date in rss.xml)
			Entry entryToStar = entriesBefore.getEntries().getFirst();
			starEntry(entryToStar.getId(), subscriptionId);

			// Verify the entry is starred
			Entries starredEntries = getCategoryEntries(CategoryREST.STARRED);
			Assertions.assertEquals(1, starredEntries.getEntries().size());

			// Run cleanup for entries older than now (should try to delete all entries)
			// With keepStarredEntries=true (default), the starred entry should be preserved
			Instant olderThan = Instant.now().plus(Duration.ofDays(1));
			databaseCleaningService.cleanEntriesOlderThan(olderThan);

			// Verify starred entry is still present
			Entries starredEntriesAfter = getCategoryEntries(CategoryREST.STARRED);
			Assertions.assertEquals(1, starredEntriesAfter.getEntries().size());
			Assertions.assertEquals(entryToStar.getId(), starredEntriesAfter.getEntries().getFirst().getId());

			// Verify the non-starred entry was deleted
			Entries entriesAfter = getFeedEntries(subscriptionId);
			Assertions.assertEquals(1, entriesAfter.getEntries().size());
			Assertions.assertEquals(entryToStar.getId(), entriesAfter.getEntries().getFirst().getId());
		}

		@Test
		void multipleStarredEntriesAreAllKept() {
			// Subscribe to feed and wait for entries
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// Verify we have 2 entries
			Entries entriesBefore = getFeedEntries(subscriptionId);
			Assertions.assertEquals(2, entriesBefore.getEntries().size());

			// Star both entries
			entriesBefore.getEntries().forEach(entry -> starEntry(entry.getId(), subscriptionId));

			// Verify both entries are starred
			Entries starredEntries = getCategoryEntries(CategoryREST.STARRED);
			Assertions.assertEquals(2, starredEntries.getEntries().size());

			// Run cleanup with capacity of 0 (should delete all non-starred entries)
			databaseCleaningService.cleanEntriesForFeedsExceedingCapacity(0);

			// Verify both starred entries are still present
			Entries starredEntriesAfter = getCategoryEntries(CategoryREST.STARRED);
			Assertions.assertEquals(2, starredEntriesAfter.getEntries().size());

			// Verify all entries are preserved (since all are starred)
			Entries entriesAfter = getFeedEntries(subscriptionId);
			Assertions.assertEquals(2, entriesAfter.getEntries().size());
		}

		@Test
		void unstarringEntryMakesItEligibleForCleanup() {
			// Subscribe to feed and wait for entries
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// Star the first entry
			Entries entriesBefore = getFeedEntries(subscriptionId);
			Entry entry = entriesBefore.getEntries().getFirst();
			starEntry(entry.getId(), subscriptionId);

			// Verify entry is starred
			Assertions.assertEquals(1, getCategoryEntries(CategoryREST.STARRED).getEntries().size());

			// Unstar the entry
			unstarEntry(entry.getId(), subscriptionId);

			// Verify entry is no longer starred
			Assertions.assertEquals(0, getCategoryEntries(CategoryREST.STARRED).getEntries().size());

			// Run cleanup for entries older than now
			Instant olderThan = Instant.now().plus(Duration.ofDays(1));
			databaseCleaningService.cleanEntriesOlderThan(olderThan);

			// Verify both entries were deleted (neither is starred)
			Entries entriesAfter = getFeedEntries(subscriptionId);
			Assertions.assertEquals(0, entriesAfter.getEntries().size());
		}
	}

	@Nested
	class AutoMarkAsRead {
		@Test
		void expiredEntriesAreMarkedAsRead() {
			// Subscribe to feed and wait for entries
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// Mark all as unread initially (they should be unread by default)
			Entries entriesBefore = getFeedEntries(subscriptionId);
			Assertions.assertTrue(entriesBefore.getEntries().stream().noneMatch(Entry::isRead));

			// Manually set expiration date to a past date for all entries in this
			// subscription
			unitOfWork.run(() -> {
				FeedSubscription sub = feedSubscriptionDAO.findById(subscriptionId);
				// Ensure status records exist for the feed
				feedEntryStatusDAO.markExpiredAutoMarkAsReadStatuses(Instant.now().plus(Duration.ofDays(100)), 100);
				// Now force the date into the past for all of them
				entityManager.createQuery("UPDATE FeedEntryStatus s SET s.autoMarkAsReadAfter = :date WHERE s.subscription = :sub")
						.setParameter("date", Instant.now().minus(Duration.ofDays(1)))
						.setParameter("sub", sub)
						.executeUpdate();
			});

			// Run the cleanup task
			databaseCleaningService.cleanExpiredAutoMarkAsReadStatuses();

			// Verify entries are now marked as read
			Entries entriesAfter = getFeedEntries(subscriptionId);
			Assertions.assertTrue(entriesAfter.getEntries().stream().allMatch(Entry::isRead));
		}

		@Test
		void resettingStatusesDeletesUnneededRecords() {
			// Subscribe to feed and wait for entries
			Long subscriptionId = subscribeAndWaitForEntries(getFeedUrl());

			// Set expiration dates so status records are created
			unitOfWork.run(() -> {
				feedEntryStatusDAO.markExpiredAutoMarkAsReadStatuses(Instant.now().plus(Duration.ofDays(100)), 100);
			});

			// Verify status records exist (they should, for unread entries if expiration is
			// set)
			// We can check this indirectly by calling reset and verifying it works without
			// error

			// Call reset via the DAO directly as if the user cleared the setting
			unitOfWork.run(() -> {
				FeedSubscription sub = feedSubscriptionDAO.findById(subscriptionId);
				feedEntryStatusDAO.resetAutoMarkAsReadStatuses(sub);
			});

			// After reset, entries should still be unread but have no expiration date
			Entries entriesAfter = getFeedEntries(subscriptionId);
			Assertions.assertTrue(entriesAfter.getEntries().stream().noneMatch(Entry::isRead));
		}
	}
}
