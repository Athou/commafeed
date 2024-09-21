package com.commafeed;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import com.commafeed.backend.feed.FeedRefreshIntervalCalculator;

import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.configuration.MemorySize;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * CommaFeed configuration
 *
 * Default values are for production, they can be overridden in application.properties for other profiles
 */
@ConfigMapping(prefix = "commafeed")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface CommaFeedConfiguration {
	/**
	 * Whether to expose a robots.txt file that disallows web crawlers and search engine indexers.
	 */
	@WithDefault("true")
	boolean hideFromWebCrawlers();

	/**
	 * If enabled, images in feed entries will be proxied through the server instead of accessed directly by the browser.
	 * 
	 * This is useful if commafeed is accessed through a restricting proxy that blocks some feeds that are followed.
	 */
	@WithDefault("false")
	boolean imageProxyEnabled();

	/**
	 * Enable password recovery via email.
	 *
	 * Quarkus mailer will need to be configured.
	 */
	@WithDefault("false")
	boolean passwordRecoveryEnabled();

	/**
	 * Message displayed in a notification at the bottom of the page.
	 */
	Optional<String> announcement();

	/**
	 * Google Analytics tracking code.
	 */
	Optional<String> googleAnalyticsTrackingCode();

	/**
	 * Google Auth key for fetching Youtube channel favicons.
	 */
	Optional<String> googleAuthKey();

	/**
	 * HTTP client configuration
	 */
	@ConfigDocSection
	HttpClient httpClient();

	/**
	 * Feed refresh engine settings.
	 */
	@ConfigDocSection
	FeedRefresh feedRefresh();

	/**
	 * Database settings.
	 */
	@ConfigDocSection
	Database database();

	/**
	 * Users settings.
	 */
	@ConfigDocSection
	Users users();

	/**
	 * Websocket settings.
	 */
	@ConfigDocSection
	Websocket websocket();

	interface HttpClient {
		/**
		 * User-Agent string that will be used by the http client, leave empty for the default one.
		 */
		Optional<String> userAgent();

		/**
		 * Time to wait for a connection to be established.
		 */
		@WithDefault("5s")
		Duration connectTimeout();

		/**
		 * Time to wait for SSL handshake to complete.
		 */
		@WithDefault("5s")
		Duration sslHandshakeTimeout();

		/**
		 * Time to wait between two packets before timeout.
		 */
		@WithDefault("10s")
		Duration socketTimeout();

		/**
		 * Time to wait for the full response to be received.
		 */
		@WithDefault("10s")
		Duration responseTimeout();

		/**
		 * Time to live for a connection in the pool.
		 */
		@WithDefault("30s")
		Duration connectionTimeToLive();

		/**
		 * Time between eviction runs for idle connections.
		 */
		@WithDefault("1m")
		Duration idleConnectionsEvictionInterval();

		/**
		 * If a feed is larger than this, it will be discarded to prevent memory issues while parsing the feed.
		 */
		@WithDefault("5M")
		MemorySize maxResponseSize();

		/**
		 * HTTP client cache configuration
		 */
		@ConfigDocSection
		HttpClientCache cache();
	}

	interface HttpClientCache {
		/**
		 * Whether to enable the cache. This cache is used to avoid spamming feeds in short bursts (e.g. when subscribing to a feed for the
		 * first time or when clicking "fetch all my feeds now").
		 */
		@WithDefault("true")
		boolean enabled();

		/**
		 * Maximum amount of memory the cache can use.
		 */
		@WithDefault("10M")
		MemorySize maximumMemorySize();

		/**
		 * Duration after which an entry is removed from the cache.
		 */
		@WithDefault("1m")
		Duration expiration();
	}

	interface FeedRefresh {
		/**
		 * Amount of time CommaFeed will wait before refreshing the same feed.
		 */
		@WithDefault("5m")
		Duration interval();

		/**
		 * If true, CommaFeed will calculate the next refresh time based on the feed's average time between entries and the time since the
		 * last entry was published. The interval will be somewhere between the default refresh interval and 24h.
		 * 
		 * See {@link FeedRefreshIntervalCalculator} for details.
		 */
		@WithDefault("false")
		boolean intervalEmpirical();

		/**
		 * Amount of http threads used to fetch feeds.
		 */
		@Min(1)
		@WithDefault("3")
		int httpThreads();

		/**
		 * Amount of threads used to insert new entries in the database.
		 */
		@Min(1)
		@WithDefault("1")
		int databaseThreads();

		/**
		 * Duration after which a user is considered inactive. Feeds for inactive users are not refreshed until they log in again.
		 *
		 * 0 to disable.
		 */
		@WithDefault("0")
		Duration userInactivityPeriod();

		/**
		 * Duration after which the evaluation of a filtering expresion to mark an entry as read is considered to have timed out.
		 */
		@WithDefault("500ms")
		Duration filteringExpressionEvaluationTimeout();

		/**
		 * Duration after which the "Fetch all my feeds now" action is available again after use to avoid spamming feeds.
		 */
		@WithDefault("0")
		Duration forceRefreshCooldownDuration();
	}

	interface Database {
		/**
		 * Timeout applied to all database queries.
		 *
		 * 0 to disable.
		 */
		@WithDefault("0")
		Duration queryTimeout();

		/**
		 * Database cleanup settings.
		 */
		@ConfigDocSection
		Cleanup cleanup();

		interface Cleanup {
			/**
			 * Maximum age of feed entries in the database. Older entries will be deleted.
			 *
			 * 0 to disable.
			 */
			@WithDefault("365d")
			Duration entriesMaxAge();

			/**
			 * Maximum age of feed entry statuses (read/unread) in the database. Older statuses will be deleted.
			 *
			 * 0 to disable.
			 */
			@WithDefault("0")
			Duration statusesMaxAge();

			/**
			 * Maximum number of entries per feed to keep in the database.
			 *
			 * 0 to disable.
			 */
			@WithDefault("500")
			int maxFeedCapacity();

			/**
			 * Limit the number of feeds a user can subscribe to.
			 *
			 * 0 to disable.
			 */
			@WithDefault("0")
			int maxFeedsPerUser();

			/**
			 * Rows to delete per query while cleaning up old entries.
			 */
			@Positive
			@WithDefault("100")
			int batchSize();

			default Instant statusesInstantThreshold() {
				return statusesMaxAge().toMillis() > 0 ? Instant.now().minus(statusesMaxAge()) : null;
			}
		}
	}

	interface Users {
		/**
		 * Whether to let users create accounts for themselves.
		 */
		@WithDefault("false")
		boolean allowRegistrations();

		/**
		 * Whether to enable strict password validation (1 uppercase char, 1 lowercase char, 1 digit, 1 special char).
		 */
		@WithDefault("true")
		boolean strictPasswordPolicy();

		/**
		 * Whether to create a demo account the first time the app starts.
		 */
		@WithDefault("false")
		boolean createDemoAccount();
	}

	interface Websocket {
		/**
		 * Enable websocket connection so the server can notify web clients that there are new entries for feeds.
		 */
		@WithDefault("true")
		boolean enabled();

		/**
		 * Interval at which the client will send a ping message on the websocket to keep the connection alive.
		 */
		@WithDefault("15m")
		Duration pingInterval();

		/**
		 * If the websocket connection is disabled or the connection is lost, the client will reload the feed tree at this interval.
		 */
		@WithDefault("30s")
		Duration treeReloadInterval();
	}

}
