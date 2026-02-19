package com.commafeed.backend.feed;

import java.util.List;

import jakarta.inject.Singleton;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.UnitOfWork;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.service.PushNotificationService;
import com.commafeed.frontend.ws.WebSocketMessageBuilder;
import com.commafeed.frontend.ws.WebSocketSessions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class FeedUpdateNotifier {

	private final CommaFeedConfiguration config;
	private final UnitOfWork unitOfWork;
	private final UserSettingsDAO userSettingsDAO;
	private final WebSocketSessions webSocketSessions;
	private final PushNotificationService pushNotificationService;

	public void notifyOverWebsocket(FeedSubscription sub, List<FeedEntry> entries) {
		if (!entries.isEmpty()) {
			webSocketSessions.sendMessage(sub.getUser(), WebSocketMessageBuilder.newFeedEntries(sub, entries.size()));
		}
	}

	public void sendPushNotifications(FeedSubscription sub, List<FeedEntry> entries) {
		if (!config.pushNotifications().enabled() || !sub.isPushNotificationsEnabled() || entries.isEmpty()) {
			return;
		}

		UserSettings settings = unitOfWork.call(() -> userSettingsDAO.findByUser(sub.getUser()));
		if (settings != null && settings.getPushNotifications() != null && settings.getPushNotifications().getType() != null) {
			for (FeedEntry entry : entries) {
				pushNotificationService.notify(settings.getPushNotifications(), sub, entry);
			}
		}
	}

}
