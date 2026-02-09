package com.commafeed;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import com.commafeed.backend.feed.FeedRefreshIntervalCalculator;

import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.configuration.MemorySize;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

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
		 * Prevent access to local addresses to mitigate server-side request forgery (SSRF) attacks, which could potentially expose internal
		 * resources.
		 *
		 * You may want to disable this if you subscribe to feeds that are only available on your local network and you trust all users of
		 * your CommaFeed instance.
		 */
		@WithDefault("true")
		boolean blockLocalAddresses();

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
		 * Default amount of time CommaFeed will wait before refreshing a feed.
		 */
		@WithDefault("5m")
		Duration interval();

		/**
		 * Maximum amount of time CommaFeed will wait before refreshing a feed. This is used as an upper bound when:
		 *
		 * <ul>
		 * <li>an error occurs while refreshing a feed and we're backing off exponentially</li>
		 * <li>we receive a Cache-Control header from the feed</li>
		 * <li>we receive a Retry-After header from the feed</li>
		 * </ul>
		 */
		@WithDefault("4h")
		Duration maxInterval();

		/**
		 * If enabled, CommaFeed will calculate the next refresh time based on the feed's average time between entries and the time since
		 * the last entry was published. The interval will be sometimes between the default refresh interval
		 * (`commafeed.feed-refresh.interval`) and the maximum refresh interval (`commafeed.feed-refresh.max-interval`).
		 * 
		 * See {@link FeedRefreshIntervalCalculator} for details.
		 */
		@WithDefault("true")
		boolean intervalEmpirical();

		/**
		 * Feed refresh engine error handling settings.
		 */
		@ConfigDocSection
		FeedRefreshErrorHandling errors();

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

	interface FeedRefreshErrorHandling {
		/**
		 * Number of retries before backoff is applied.
		 */
		@Min(0)
		@WithDefault("3")
		int retriesBeforeBackoff();

		/**
		 * Duration to wait before retrying after an error. Will be multiplied by the number of errors since the last successful fetch.
		 */
		@WithDefault("1h")
		Duration backoffInterval();
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

			/**
			 * Whether to keep starred entries when cleaning up old entries.
			 */
			@WithDefault("true")
			boolean keepStarredEntries();

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
		 * Minimum password length for user accounts.
		 */
		@WithDefault("4")
		int minimumPasswordLength();

		/**
		 * Whether an email address is required when creating a user account.
		 */
		@WithDefault("false")
		boolean emailAddressRequired();

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
