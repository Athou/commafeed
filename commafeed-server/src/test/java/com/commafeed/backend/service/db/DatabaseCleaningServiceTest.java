package com.commafeed.backend.service.db;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryDAO.FeedCapacity;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.model.Feed;

@ExtendWith(MockitoExtension.class)
class DatabaseCleaningServiceTest {

	private static final int BATCH_SIZE = 100;

	@Mock
	private CommaFeedConfiguration config;

	@Mock
	private CommaFeedConfiguration.Database databaseConfig;

	@Mock
	private CommaFeedConfiguration.Database.Cleanup cleaningConfig;

	@Mock
	private UnitOfWork unitOfWork;

	@Mock
	private FeedDAO feedDAO;

	@Mock
	private FeedEntryDAO feedEntryDAO;

	@Mock
	private FeedEntryContentDAO feedEntryContentDAO;

	@Mock
	private FeedEntryStatusDAO feedEntryStatusDAO;

	@Mock
	private MetricRegistry metrics;

	@Mock
	private Meter entriesDeletedMeter;

	private DatabaseCleaningService service;

	@BeforeEach
	void setUp() {
		Mockito.when(config.database()).thenReturn(databaseConfig);
		Mockito.when(databaseConfig.cleanup()).thenReturn(cleaningConfig);
		Mockito.when(cleaningConfig.batchSize()).thenReturn(BATCH_SIZE);
		Mockito.when(metrics.meter(Mockito.anyString())).thenReturn(entriesDeletedMeter);

		Mockito.when(unitOfWork.call(Mockito.any())).thenAnswer(invocation -> ((Callable<?>) invocation.getArgument(0)).call());

		service = new DatabaseCleaningService(config, unitOfWork, feedDAO, feedEntryDAO, feedEntryContentDAO, feedEntryStatusDAO, metrics);
	}

	@Test
	void cleanFeedsWithoutSubscriptionsDeletesFeedsAndEntries() {
		Feed feed1 = Mockito.mock(Feed.class);
		Feed feed2 = Mockito.mock(Feed.class);
		Mockito.when(feed1.getId()).thenReturn(1L);
		Mockito.when(feed2.getId()).thenReturn(2L);

		// First iteration returns feeds, second returns empty list to terminate loop
		Mockito.when(feedDAO.findWithoutSubscriptions(Mockito.anyInt()))
				.thenReturn(Arrays.asList(feed1, feed2))
				.thenReturn(Collections.emptyList());

		Mockito.when(feedEntryDAO.delete(1L, BATCH_SIZE)).thenReturn(10, 0);
		Mockito.when(feedEntryDAO.delete(2L, BATCH_SIZE)).thenReturn(5, 0);
		Mockito.when(feedDAO.delete(Mockito.anyList())).thenReturn(2, 0);

		service.cleanFeedsWithoutSubscriptions();

		Mockito.verify(entriesDeletedMeter, Mockito.times(4)).mark(Mockito.anyLong());
		Mockito.verify(feedDAO, Mockito.times(2)).delete(Mockito.anyList());
	}

	@Test
	void cleanContentsWithoutEntriesDeletesContents() {
		Mockito.when(feedEntryContentDAO.deleteWithoutEntries(Mockito.anyInt())).thenReturn(50L, 30L, 0L);

		service.cleanContentsWithoutEntries();

		Mockito.verify(feedEntryContentDAO, Mockito.times(3)).deleteWithoutEntries(Mockito.anyInt());
	}

	@Test
	void cleanEntriesForFeedsExceedingCapacityDeletesOldEntries() {
		FeedCapacity feed1 = Mockito.mock(FeedCapacity.class);
		Mockito.when(feed1.id()).thenReturn(1L);
		Mockito.when(feed1.capacity()).thenReturn(180L);

		FeedCapacity feed2 = Mockito.mock(FeedCapacity.class);
		Mockito.when(feed2.id()).thenReturn(2L);
		Mockito.when(feed2.capacity()).thenReturn(120L);

		Mockito.when(feedEntryDAO.findFeedsExceedingCapacity(50, BATCH_SIZE, false))
				.thenReturn(Arrays.asList(feed1, feed2))
				.thenReturn(Collections.emptyList());

		Mockito.when(feedEntryDAO.deleteOldEntries(1L, 100, false)).thenReturn(80);
		Mockito.when(feedEntryDAO.deleteOldEntries(1L, 50, false)).thenReturn(50);
		Mockito.when(feedEntryDAO.deleteOldEntries(2L, 70, false)).thenReturn(70);

		service.cleanEntriesForFeedsExceedingCapacity(50);

		Mockito.verify(entriesDeletedMeter, Mockito.times(3)).mark(Mockito.anyLong());
	}

	@Test
	void cleanEntriesOlderThanDeletesOldEntries() {
		Instant cutoff = LocalDate.now().minusDays(30).atStartOfDay().toInstant(ZoneOffset.UTC);

		Mockito.when(feedEntryDAO.deleteEntriesOlderThan(cutoff, BATCH_SIZE, false)).thenReturn(100, 50, 0);

		service.cleanEntriesOlderThan(cutoff);

		Mockito.verify(feedEntryDAO, Mockito.times(3)).deleteEntriesOlderThan(cutoff, BATCH_SIZE, false);
		Mockito.verify(entriesDeletedMeter, Mockito.times(3)).mark(Mockito.anyLong());
	}

	@Test
	void cleanStatusesOlderThanDeletesOldStatuses() {
		Instant cutoff = LocalDate.now().minusDays(60).atStartOfDay().toInstant(ZoneOffset.UTC);

		Mockito.when(feedEntryStatusDAO.deleteOldStatuses(cutoff, BATCH_SIZE)).thenReturn(200L, 100L, 0L);

		service.cleanStatusesOlderThan(cutoff);

		Mockito.verify(feedEntryStatusDAO, Mockito.times(3)).deleteOldStatuses(cutoff, BATCH_SIZE);
	}
}