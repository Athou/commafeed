package com.commafeed.config;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import io.quarkus.runtime.configuration.MemorySize;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * CommaFeed configuration
 *
 * Default values are for production, they can be overridden in application.properties
 */
@ConfigMapping(prefix = "cf.app")
public interface CommaFeedConfiguration {

	enum CacheType {
		NOOP, REDIS
	}

	@NotBlank
	@WithDefault("http://localhost:8082")
	String publicUrl();

	@WithDefault("true")
	boolean hideFromWebCrawlers();

	@WithDefault("false")
	boolean allowRegistrations();

	@WithDefault("true")
	boolean strictPasswordPolicy();

	@WithDefault("false")
	boolean createDemoAccount();

	Optional<String> googleAnalyticsTrackingCode();

	Optional<String> googleAuthKey();

	@Min(1)
	@WithDefault("3")
	int httpThreads();

	@Min(1)
	@WithDefault("1")
	int databaseUpdateThreads();

	@Positive
	@WithDefault("100")
	int databaseCleanupBatchSize();

	Optional<Smtp> smtp();

	@WithDefault("false")
	boolean heavyLoad();

	@WithDefault("false")
	boolean imageProxyEnabled();

	@WithDefault("0")
	int databaseQueryTimeout();

	@WithDefault("0")
	Duration keepStatus();

	@WithDefault("500")
	int maxFeedCapacity();

	@WithDefault("365")
	int maxEntriesAgeDays();

	@WithDefault("0")
	int maxFeedsPerUser();

	@WithDefault("5M")
	MemorySize maxFeedResponseSize();

	@WithDefault("5m")
	Duration feedRefreshInterval();

	@WithDefault("NOOP")
	CacheType cache();

	Optional<String> announcement();

	Optional<String> userAgent();

	Websocket websocket();

	default Instant getUnreadThreshold() {
		return keepStatus().toMillis() > 0 ? Instant.now().minus(keepStatus()) : null;
	}

	interface Smtp {
		String host();

		int port();

		boolean tls();

		String userName();

		String password();

		String fromAddress();
	}

	interface Websocket {
		@WithDefault("true")
		boolean enabled();

		@WithDefault("15m")
		Duration pingInterval();

		@WithDefault("30s")
		Duration treeReloadInterval();
	}

}
