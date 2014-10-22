package com.commafeed.frontend.session;

import io.dropwizard.util.Duration;

import java.io.File;
import java.io.IOException;

import javax.servlet.SessionTrackingMode;

import lombok.Getter;

import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.HashSessionManager;

import com.google.common.collect.ImmutableSet;

@Getter
public class SessionManagerFactory {

	private String path = "sessions";
	private Duration cookieMaxAge = Duration.days(30);
	private Duration cookieRefreshAge = Duration.days(1);
	private Duration maxInactiveInterval = Duration.days(30);
	private Duration idleSavePeriod = Duration.hours(2);
	private Duration savePeriod = Duration.minutes(5);
	private Duration scavengePeriod = Duration.minutes(5);

	public SessionManager build() throws IOException {
		HashSessionManager manager = new HashSessionManager();
		manager.setSessionTrackingModes(ImmutableSet.of(SessionTrackingMode.COOKIE));
		manager.setHttpOnly(true);
		manager.getSessionCookieConfig().setHttpOnly(true);
		manager.setDeleteUnrestorableSessions(true);

		manager.setStoreDirectory(new File(getPath()));
		manager.getSessionCookieConfig().setMaxAge((int) cookieMaxAge.toSeconds());
		manager.setRefreshCookieAge((int) cookieRefreshAge.toSeconds());
		manager.setMaxInactiveInterval((int) maxInactiveInterval.toSeconds());
		manager.setIdleSavePeriod((int) idleSavePeriod.toSeconds());
		manager.setSavePeriod((int) savePeriod.toSeconds());
		manager.setScavengePeriod((int) scavengePeriod.toSeconds());
		return manager;
	}

}
