package com.commafeed.backend.feed;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.feed.parser.FeedParserResult.Content;
import com.commafeed.backend.feed.parser.FeedParserResult.Entry;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedService;
import com.commafeed.backend.service.NotificationService;
import com.commafeed.frontend.ws.WebSocketSessions;

@ExtendWith(MockitoExtension.class)
class FeedRefreshUpdaterTest {

	@Mock
	private UnitOfWork unitOfWork;

	@Mock
	private FeedService feedService;

	@Mock
	private FeedEntryService feedEntryService;

	@Mock
	private FeedSubscriptionDAO feedSubscriptionDAO;

	@Mock
	private UserSettingsDAO userSettingsDAO;

	@Mock
	private WebSocketSessions webSocketSessions;

	@Mock
	private NotificationService notificationService;

	private FeedRefreshUpdater updater;

	private Feed feed;
	private User user;
	private FeedSubscription subscription;
	private Entry entry;
	private FeedEntry feedEntry;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() throws Exception {
		MetricRegistry metrics = new MetricRegistry();
		updater = new FeedRefreshUpdater(unitOfWork, feedService, feedEntryService, metrics, feedSubscriptionDAO, userSettingsDAO,
				webSocketSessions, notificationService);

		// UnitOfWork passthrough: execute callables and runnables directly
		Mockito.when(unitOfWork.call(Mockito.any())).thenAnswer(inv -> inv.getArgument(0, Callable.class).call());
		Mockito.doAnswer(inv -> {
			inv.getArgument(0, Runnable.class).run();
			return null;
		}).when(unitOfWork).run(Mockito.any());

		user = new User();
		user.setId(1L);

		feed = new Feed();
		feed.setId(1L);
		feed.setUrl("https://example.com/feed.xml");

		subscription = new FeedSubscription();
		subscription.setId(1L);
		subscription.setTitle("My Feed");
		subscription.setUser(user);
		subscription.setNotifyOnNewEntries(true);

		Content content = new Content("Article Title", "content", "author", null, null, null);
		entry = new Entry("guid-1", "https://example.com/article", Instant.now(), content);

		feedEntry = new FeedEntry();
		feedEntry.setUrl("https://example.com/article");
	}

	@Test
	void updateSendsNotificationsForNewEntries() {
		Mockito.when(feedSubscriptionDAO.findByFeed(feed)).thenReturn(List.of(subscription));
		Mockito.when(feedEntryService.find(feed, entry)).thenReturn(null);
		Mockito.when(feedEntryService.create(feed, entry)).thenReturn(feedEntry);
		Mockito.when(feedEntryService.applyFilter(subscription, feedEntry)).thenReturn(true);

		UserSettings settings = new UserSettings();
		settings.setNotificationEnabled(true);
		Mockito.when(userSettingsDAO.findByUser(user)).thenReturn(settings);

		updater.update(feed, List.of(entry));

		Mockito.verify(notificationService).notify(settings, subscription, feedEntry);
	}

	@Test
	void updateDoesNotNotifyWhenSubscriptionNotifyDisabled() {
		subscription.setNotifyOnNewEntries(false);

		Mockito.when(feedSubscriptionDAO.findByFeed(feed)).thenReturn(List.of(subscription));
		Mockito.when(feedEntryService.find(feed, entry)).thenReturn(null);
		Mockito.when(feedEntryService.create(feed, entry)).thenReturn(feedEntry);
		Mockito.when(feedEntryService.applyFilter(subscription, feedEntry)).thenReturn(true);

		updater.update(feed, List.of(entry));

		Mockito.verify(notificationService, Mockito.never()).notify(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void updateDoesNotNotifyWhenUserNotificationsDisabled() {
		Mockito.when(feedSubscriptionDAO.findByFeed(feed)).thenReturn(List.of(subscription));
		Mockito.when(feedEntryService.find(feed, entry)).thenReturn(null);
		Mockito.when(feedEntryService.create(feed, entry)).thenReturn(feedEntry);
		Mockito.when(feedEntryService.applyFilter(subscription, feedEntry)).thenReturn(true);

		UserSettings settings = new UserSettings();
		settings.setNotificationEnabled(false);
		Mockito.when(userSettingsDAO.findByUser(user)).thenReturn(settings);

		updater.update(feed, List.of(entry));

		Mockito.verify(notificationService, Mockito.never()).notify(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void updateDoesNotNotifyWhenNoUserSettings() {
		Mockito.when(feedSubscriptionDAO.findByFeed(feed)).thenReturn(List.of(subscription));
		Mockito.when(feedEntryService.find(feed, entry)).thenReturn(null);
		Mockito.when(feedEntryService.create(feed, entry)).thenReturn(feedEntry);
		Mockito.when(feedEntryService.applyFilter(subscription, feedEntry)).thenReturn(true);

		Mockito.when(userSettingsDAO.findByUser(user)).thenReturn(null);

		updater.update(feed, List.of(entry));

		Mockito.verify(notificationService, Mockito.never()).notify(Mockito.any(), Mockito.any(), Mockito.any());
	}

	@Test
	void updateDoesNotNotifyForExistingEntries() {
		Mockito.when(feedSubscriptionDAO.findByFeed(feed)).thenReturn(List.of(subscription));
		Mockito.when(feedEntryService.find(feed, entry)).thenReturn(feedEntry);

		updater.update(feed, List.of(entry));

		Mockito.verify(notificationService, Mockito.never()).notify(Mockito.any(), Mockito.any(), Mockito.any());
	}
}
