package com.commafeed.frontend.session;

import java.io.File;

import javax.servlet.SessionTrackingMode;

import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.FileSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;

import com.google.common.collect.ImmutableSet;

import io.dropwizard.util.Duration;

public class SessionHandlerFactory {

	private String path = "sessions";
	private Duration cookieMaxAge = Duration.days(30);
	private Duration cookieRefreshAge = Duration.days(1);
	private Duration maxInactiveInterval = Duration.days(30);
	private Duration savePeriod = Duration.minutes(5);

	public SessionHandler build() {
		SessionHandler sessionHandler = new SessionHandler() {
			{
				// no setter available for maxCookieAge
				_maxCookieAge = (int) cookieMaxAge.toSeconds();
			}
		};
		SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
		sessionHandler.setSessionCache(sessionCache);
		FileSessionDataStore dataStore = new FileSessionDataStore();
		sessionCache.setSessionDataStore(dataStore);

		sessionHandler.setHttpOnly(true);
		sessionHandler.setSessionTrackingModes(ImmutableSet.of(SessionTrackingMode.COOKIE));
		sessionHandler.setMaxInactiveInterval((int) maxInactiveInterval.toSeconds());
		sessionHandler.setRefreshCookieAge((int) cookieRefreshAge.toSeconds());

		dataStore.setDeleteUnrestorableFiles(true);
		dataStore.setStoreDir(new File(path));
		dataStore.setSavePeriodSec((int) savePeriod.toSeconds());

		return sessionHandler;
	}

}
