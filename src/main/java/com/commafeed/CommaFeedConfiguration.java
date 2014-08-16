package com.commafeed;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import java.util.Date;
import java.util.ResourceBundle;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Getter;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.validator.constraints.NotBlank;

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
	private DataSourceFactory database = new DataSourceFactory();

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
		@JsonProperty
		@NotNull
		@NotBlank
		private String contextPath;

		@JsonProperty
		@NotNull
		@NotBlank
		private String publicUrl;

		@JsonProperty
		@NotNull
		private boolean allowRegistrations;

		@JsonProperty
		private String googleAnalyticsTrackingCode;

		@JsonProperty
		@NotNull
		@Min(1)
		private int backgroundThreads;

		@JsonProperty
		@NotNull
		@Min(1)
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
		@NotNull
		private boolean heavyLoad;

		@JsonProperty
		@NotNull
		private boolean pubsubhubbub;

		@JsonProperty
		@NotNull
		private boolean imageProxyEnabled;

		@JsonProperty
		@NotNull
		@Min(0)
		private int queryTimeout;

		@JsonProperty
		@NotNull
		@Min(0)
		private int keepStatusDays;

		@JsonProperty
		@NotNull
		@Min(0)
		private int refreshIntervalMinutes;

		@JsonProperty
		@NotNull
		private CacheType cache;

		@JsonProperty
		@NotNull
		private String announcement;

		public Date getUnreadThreshold() {
			int keepStatusDays = getKeepStatusDays();
			return keepStatusDays > 0 ? DateUtils.addDays(new Date(), -1 * keepStatusDays) : null;
		}

	}

}
