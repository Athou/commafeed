package com.commafeed.frontend.session;

import javax.servlet.SessionTrackingMode;

import org.eclipse.jetty.server.session.DatabaseAdaptor;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.JDBCSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.util.Duration;

public class SessionHandlerFactory {

	@JsonProperty
	private Duration cookieMaxAge = Duration.days(30);

	@JsonProperty
	private Duration cookieRefreshAge = Duration.days(1);

	@JsonProperty
	private Duration maxInactiveInterval = Duration.days(30);

	@JsonProperty
	private Duration savePeriod = Duration.minutes(5);

	public SessionHandler build(DataSourceFactory dataSourceFactory) {
		SessionHandler sessionHandler = new SessionHandler();
		sessionHandler.setHttpOnly(true);
		sessionHandler.setSessionTrackingModes(ImmutableSet.of(SessionTrackingMode.COOKIE));
		sessionHandler.setMaxInactiveInterval((int) maxInactiveInterval.toSeconds());
		sessionHandler.setRefreshCookieAge((int) cookieRefreshAge.toSeconds());
		sessionHandler.getSessionCookieConfig().setMaxAge((int) cookieMaxAge.toSeconds());

		SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
		sessionHandler.setSessionCache(sessionCache);

		JDBCSessionDataStore dataStore = new JDBCSessionDataStore();
		dataStore.setSavePeriodSec((int) savePeriod.toSeconds());
		sessionCache.setSessionDataStore(dataStore);

		DatabaseAdaptor adaptor = new DatabaseAdaptor();
		adaptor.setDatasource(dataSourceFactory.build(new MetricRegistry(), "sessions"));
		dataStore.setDatabaseAdaptor(adaptor);

		return sessionHandler;
	}

}
