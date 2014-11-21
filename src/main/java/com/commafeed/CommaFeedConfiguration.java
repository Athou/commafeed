package com.commafeed;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import java.util.Date;
import java.util.ResourceBundle;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Getter;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.commafeed.backend.cache.RedisPoolFactory;
import com.commafeed.frontend.session.SessionManagerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
public class CommaFeedConfiguration extends Configuration {

	public static enum CacheType {
		NOOP, REDIS
	}

	private ResourceBundle bundle;

	public CommaFeedConfiguration() {
		bundle = ResourceBundle.getBundle("application");
	}

	@Valid
	@NotNull
	@JsonProperty("database")
	private DataSourceFactory dataSourceFactory = new DataSourceFactory();

	@Valid
	@NotNull
	@JsonProperty("redis")
	private RedisPoolFactory redisPoolFactory = new RedisPoolFactory();

	@Valid
	@NotNull
	@JsonProperty("session")
	private SessionManagerFactory sessionManagerFactory = new SessionManagerFactory();

	@Valid
	@NotNull
	@JsonProperty("app")
	private ApplicationSettings applicationSettings;

	public String getVersion() {
		return bundle.getString("version");
	}

	public String getGitCommit() {
		return bundle.getString("git.commit");
	}

	@Getter
	public static class ApplicationSettings {
		@NotNull
		@NotBlank
		private String publicUrl;

		@NotNull
		private boolean allowRegistrations;

		private String googleAnalyticsTrackingCode;

		@NotNull
		@Min(1)
		private int backgroundThreads;

		@NotNull
		@Min(1)
		private int databaseUpdateThreads;

		private String smtpHost;

		private int smtpPort;

		private boolean smtpTls;

		private String smtpUserName;

		private String smtpPassword;

		private String smtpFromAddress;

		@NotNull
		private boolean heavyLoad;

		@NotNull
		private boolean pubsubhubbub;

		@NotNull
		private boolean imageProxyEnabled;

		@NotNull
		@Min(0)
		private int queryTimeout;

		@NotNull
		@Min(0)
		private int keepStatusDays;

		@NotNull
		@Min(0)
		private int refreshIntervalMinutes;

		@NotNull
		private CacheType cache;

		@NotNull
		private String announcement;

		public Date getUnreadThreshold() {
			int keepStatusDays = getKeepStatusDays();
			return keepStatusDays > 0 ? DateUtils.addDays(new Date(), -1 * keepStatusDays) : null;
		}

	}

}
