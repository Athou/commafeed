package com.commafeed;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;

import org.apache.commons.lang.time.DateUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilderSpec;

@Getter
public class CommaFeedConfiguration extends Configuration {

	public static enum CacheType {
		NOOP, REDIS
	}

	@Valid
	@NotNull
	@JsonProperty("database")
	private DataSourceFactory database = new DataSourceFactory();

	@Valid
	@NotNull
	@JsonProperty("authenticationCachePolicy")
	private CacheBuilderSpec authenticationCachePolicy;

	@Valid
	@NotNull
	@JsonProperty("app")
	private ApplicationSettings applicationSettings;

	@Getter
	public static class ApplicationSettings {
		@JsonProperty
		private String publicUrl;

		@JsonProperty
		private boolean allowRegistrations;

		@JsonProperty
		private String googleAnalyticsTrackingCode;

		@JsonProperty
		private String googleClientId;

		@JsonProperty
		private String googleClientSecret;

		@JsonProperty
		private int backgroundThreads;

		@JsonProperty
		private int databaseUpdateThreads;

		@JsonProperty
		private String smtpHost;

		@JsonProperty
		private int smtpPort;

		@JsonProperty
		private boolean smtpTls;

		@JsonProperty
		private String smtpUserName;

		@JsonProperty
		private String smtpPassword;

		@JsonProperty
		private boolean heavyLoad;

		@JsonProperty
		private boolean pubsubhubbub;

		@JsonProperty
		private boolean imageProxyEnabled;

		@JsonProperty
		private int queryTimeout;

		@JsonProperty
		private boolean crawlingPaused;

		@JsonProperty
		private int keepStatusDays;

		@JsonProperty
		private int refreshIntervalMinutes;

		@JsonProperty
		private CacheType cache;

		@JsonProperty
		private String announcement;

		public Date getUnreadThreshold() {
			int keepStatusDays = getKeepStatusDays();
			return keepStatusDays > 0 ? DateUtils.addDays(new Date(), -1 * keepStatusDays) : null;
		}

	}

}
