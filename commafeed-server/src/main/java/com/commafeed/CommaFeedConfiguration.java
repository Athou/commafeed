package com.commafeed;

import java.util.Date;
import java.util.ResourceBundle;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.apache.commons.lang3.time.DateUtils;

import com.commafeed.backend.cache.RedisPoolFactory;
import com.commafeed.frontend.session.SessionHandlerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommaFeedConfiguration extends Configuration {

	public enum CacheType {
		NOOP, REDIS
	}

	@Valid
	@NotNull
	@JsonProperty("database")
	private final DataSourceFactory dataSourceFactory = new DataSourceFactory();

	@Valid
	@NotNull
	@JsonProperty("redis")
	private final RedisPoolFactory redisPoolFactory = new RedisPoolFactory();

	@Valid
	@NotNull
	@JsonProperty("session")
	private final SessionHandlerFactory sessionHandlerFactory = new SessionHandlerFactory();

	@Valid
	@NotNull
	@JsonProperty("app")
	private ApplicationSettings applicationSettings;

	private final String version;
	private final String gitCommit;

	public CommaFeedConfiguration() {
		ResourceBundle bundle = ResourceBundle.getBundle("application");

		this.version = bundle.getString("version");
		this.gitCommit = bundle.getString("git.commit");
	}

	@Getter
	@Setter
	public static class ApplicationSettings {
		@NotNull
		@NotBlank
		@Valid
		private String publicUrl;

		@NotNull
		@Valid
		private Boolean hideFromWebCrawlers = true;

		@NotNull
		@Valid
		private Boolean allowRegistrations;

		@NotNull
		@Valid
		private Boolean strictPasswordPolicy = true;

		@NotNull
		@Valid
		private Boolean createDemoAccount;

		private String googleAnalyticsTrackingCode;

		private String googleAuthKey;

		@NotNull
		@Min(1)
		@Valid
		private Integer backgroundThreads;

		@NotNull
		@Min(1)
		@Valid
		private Integer databaseUpdateThreads;

		@NotNull
		@Positive
		@Valid
		private Integer databaseCleanupBatchSize = 100;

		private String smtpHost;
		private int smtpPort;
		private boolean smtpTls;
		private String smtpUserName;
		private String smtpPassword;
		private String smtpFromAddress;

		private boolean graphiteEnabled;
		private String graphitePrefix;
		private String graphiteHost;
		private int graphitePort;
		private int graphiteInterval;

		@NotNull
		@Valid
		private Boolean heavyLoad;

		@NotNull
		@Valid
		private Boolean pubsubhubbub;

		@NotNull
		@Valid
		private Boolean imageProxyEnabled;

		@NotNull
		@Min(0)
		@Valid
		private Integer queryTimeout;

		@NotNull
		@Min(0)
		@Valid
		private Integer keepStatusDays;

		@NotNull
		@Min(0)
		@Valid
		private Integer maxFeedCapacity;

		@NotNull
		@Valid
		private Integer maxFeedsPerUser = 0;

		@NotNull
		@Min(0)
		@Valid
		private Integer refreshIntervalMinutes;

		@NotNull
		@Valid
		private CacheType cache;

		@Valid
		private String announcement;

		private String userAgent;

		public Date getUnreadThreshold() {
			int keepStatusDays = getKeepStatusDays();
			return keepStatusDays > 0 ? DateUtils.addDays(new Date(), -1 * keepStatusDays) : null;
		}

	}

}
