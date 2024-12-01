🔒: Configuration property fixed at build time - All other configuration properties are overridable at runtime

<table>
<thead>
<tr>
<th align="left">Configuration property</th>
<th>Type</th>
<th>Default</th>
</tr>
</thead>
<tbody>
<tr>
<td>

`commafeed.hide-from-web-crawlers`

Whether to expose a robots.txt file that disallows web crawlers and search engine indexers.



Environment variable: `COMMAFEED_HIDE_FROM_WEB_CRAWLERS`</td>
<td>

boolean
</td>
<td>

`true`
</td>
</tr>
<tr>
<td>

`commafeed.image-proxy-enabled`

If enabled, images in feed entries will be proxied through the server instead of accessed directly by the browser.

This is useful if commafeed is accessed through a restricting proxy that blocks some feeds that are followed.



Environment variable: `COMMAFEED_IMAGE_PROXY_ENABLED`</td>
<td>

boolean
</td>
<td>

`false`
</td>
</tr>
<tr>
<td>

`commafeed.password-recovery-enabled`

Enable password recovery via email.

Quarkus mailer will need to be configured.



Environment variable: `COMMAFEED_PASSWORD_RECOVERY_ENABLED`</td>
<td>

boolean
</td>
<td>

`false`
</td>
</tr>
<tr>
<td>

`commafeed.announcement`

Message displayed in a notification at the bottom of the page.



Environment variable: `COMMAFEED_ANNOUNCEMENT`</td>
<td>

string
</td>
<td>


</td>
</tr>
<tr>
<td>

`commafeed.google-analytics-tracking-code`

Google Analytics tracking code.



Environment variable: `COMMAFEED_GOOGLE_ANALYTICS_TRACKING_CODE`</td>
<td>

string
</td>
<td>


</td>
</tr>
<tr>
<td>

`commafeed.google-auth-key`

Google Auth key for fetching Youtube channel favicons.



Environment variable: `COMMAFEED_GOOGLE_AUTH_KEY`</td>
<td>

string
</td>
<td>


</td>
</tr>
<thead>
<tr>
<th align="left" colspan="3">
HTTP client configuration
</th>
</tr>
</thead>

<tr>
<td>

`commafeed.http-client.user-agent`

User-Agent string that will be used by the http client, leave empty for the default one.



Environment variable: `COMMAFEED_HTTP_CLIENT_USER_AGENT`</td>
<td>

string
</td>
<td>


</td>
</tr>
<tr>
<td>

`commafeed.http-client.connect-timeout`

Time to wait for a connection to be established.



Environment variable: `COMMAFEED_HTTP_CLIENT_CONNECT_TIMEOUT`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`5S`
</td>
</tr>
<tr>
<td>

`commafeed.http-client.ssl-handshake-timeout`

Time to wait for SSL handshake to complete.



Environment variable: `COMMAFEED_HTTP_CLIENT_SSL_HANDSHAKE_TIMEOUT`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`5S`
</td>
</tr>
<tr>
<td>

`commafeed.http-client.socket-timeout`

Time to wait between two packets before timeout.



Environment variable: `COMMAFEED_HTTP_CLIENT_SOCKET_TIMEOUT`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`10S`
</td>
</tr>
<tr>
<td>

`commafeed.http-client.response-timeout`

Time to wait for the full response to be received.



Environment variable: `COMMAFEED_HTTP_CLIENT_RESPONSE_TIMEOUT`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`10S`
</td>
</tr>
<tr>
<td>

`commafeed.http-client.connection-time-to-live`

Time to live for a connection in the pool.



Environment variable: `COMMAFEED_HTTP_CLIENT_CONNECTION_TIME_TO_LIVE`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`30S`
</td>
</tr>
<tr>
<td>

`commafeed.http-client.idle-connections-eviction-interval`

Time between eviction runs for idle connections.



Environment variable: `COMMAFEED_HTTP_CLIENT_IDLE_CONNECTIONS_EVICTION_INTERVAL`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`1M`
</td>
</tr>
<tr>
<td>

`commafeed.http-client.max-response-size`

If a feed is larger than this, it will be discarded to prevent memory issues while parsing the feed.



Environment variable: `COMMAFEED_HTTP_CLIENT_MAX_RESPONSE_SIZE`</td>
<td>

MemorySize [🛈](#memory-size-note-anchor)
</td>
<td>

`5M`
</td>
</tr>
<thead>
<tr>
<th align="left" colspan="3">
&nbsp;&nbsp;&nbsp;&nbsp;HTTP client cache configuration
</th>
</tr>
</thead>

<tr>
<td>

`commafeed.http-client.cache.enabled`

Whether to enable the cache. This cache is used to avoid spamming feeds in short bursts (e.g. when subscribing to a feed for the
first time or when clicking "fetch all my feeds now").



Environment variable: `COMMAFEED_HTTP_CLIENT_CACHE_ENABLED`</td>
<td>

boolean
</td>
<td>

`true`
</td>
</tr>
<tr>
<td>

`commafeed.http-client.cache.maximum-memory-size`

Maximum amount of memory the cache can use.



Environment variable: `COMMAFEED_HTTP_CLIENT_CACHE_MAXIMUM_MEMORY_SIZE`</td>
<td>

MemorySize [🛈](#memory-size-note-anchor)
</td>
<td>

`10M`
</td>
</tr>
<tr>
<td>

`commafeed.http-client.cache.expiration`

Duration after which an entry is removed from the cache.



Environment variable: `COMMAFEED_HTTP_CLIENT_CACHE_EXPIRATION`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`1M`
</td>
</tr>
<thead>
<tr>
<th align="left" colspan="3">
Feed refresh engine settings
</th>
</tr>
</thead>

<tr>
<td>

`commafeed.feed-refresh.interval`

Amount of time CommaFeed will wait before refreshing the same feed.



Environment variable: `COMMAFEED_FEED_REFRESH_INTERVAL`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`5M`
</td>
</tr>
<tr>
<td>

`commafeed.feed-refresh.interval-empirical`

If true, CommaFeed will calculate the next refresh time based on the feed's average time between entries and the time since the
last entry was published. The interval will be somewhere between the default refresh interval and 24h.

See <code>FeedRefreshIntervalCalculator</code> for details.



Environment variable: `COMMAFEED_FEED_REFRESH_INTERVAL_EMPIRICAL`</td>
<td>

boolean
</td>
<td>

`false`
</td>
</tr>
<tr>
<td>

`commafeed.feed-refresh.http-threads`

Amount of http threads used to fetch feeds.



Environment variable: `COMMAFEED_FEED_REFRESH_HTTP_THREADS`</td>
<td>

int
</td>
<td>

`3`
</td>
</tr>
<tr>
<td>

`commafeed.feed-refresh.database-threads`

Amount of threads used to insert new entries in the database.



Environment variable: `COMMAFEED_FEED_REFRESH_DATABASE_THREADS`</td>
<td>

int
</td>
<td>

`1`
</td>
</tr>
<tr>
<td>

`commafeed.feed-refresh.user-inactivity-period`

Duration after which a user is considered inactive. Feeds for inactive users are not refreshed until they log in again.

0 to disable.



Environment variable: `COMMAFEED_FEED_REFRESH_USER_INACTIVITY_PERIOD`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`0S`
</td>
</tr>
<tr>
<td>

`commafeed.feed-refresh.filtering-expression-evaluation-timeout`

Duration after which the evaluation of a filtering expresion to mark an entry as read is considered to have timed out.



Environment variable: `COMMAFEED_FEED_REFRESH_FILTERING_EXPRESSION_EVALUATION_TIMEOUT`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`500MS`
</td>
</tr>
<tr>
<td>

`commafeed.feed-refresh.force-refresh-cooldown-duration`

Duration after which the "Fetch all my feeds now" action is available again after use to avoid spamming feeds.



Environment variable: `COMMAFEED_FEED_REFRESH_FORCE_REFRESH_COOLDOWN_DURATION`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`0S`
</td>
</tr>
<thead>
<tr>
<th align="left" colspan="3">
Database settings
</th>
</tr>
</thead>

<tr>
<td>

`commafeed.database.query-timeout`

Timeout applied to all database queries.

0 to disable.



Environment variable: `COMMAFEED_DATABASE_QUERY_TIMEOUT`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`0S`
</td>
</tr>
<thead>
<tr>
<th align="left" colspan="3">
&nbsp;&nbsp;&nbsp;&nbsp;Database cleanup settings
</th>
</tr>
</thead>

<tr>
<td>

`commafeed.database.cleanup.entries-max-age`

Maximum age of feed entries in the database. Older entries will be deleted.

0 to disable.



Environment variable: `COMMAFEED_DATABASE_CLEANUP_ENTRIES_MAX_AGE`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`365D`
</td>
</tr>
<tr>
<td>

`commafeed.database.cleanup.statuses-max-age`

Maximum age of feed entry statuses (read/unread) in the database. Older statuses will be deleted.

0 to disable.



Environment variable: `COMMAFEED_DATABASE_CLEANUP_STATUSES_MAX_AGE`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`0S`
</td>
</tr>
<tr>
<td>

`commafeed.database.cleanup.max-feed-capacity`

Maximum number of entries per feed to keep in the database.

0 to disable.



Environment variable: `COMMAFEED_DATABASE_CLEANUP_MAX_FEED_CAPACITY`</td>
<td>

int
</td>
<td>

`500`
</td>
</tr>
<tr>
<td>

`commafeed.database.cleanup.max-feeds-per-user`

Limit the number of feeds a user can subscribe to.

0 to disable.



Environment variable: `COMMAFEED_DATABASE_CLEANUP_MAX_FEEDS_PER_USER`</td>
<td>

int
</td>
<td>

`0`
</td>
</tr>
<tr>
<td>

`commafeed.database.cleanup.batch-size`

Rows to delete per query while cleaning up old entries.



Environment variable: `COMMAFEED_DATABASE_CLEANUP_BATCH_SIZE`</td>
<td>

int
</td>
<td>

`100`
</td>
</tr>
<thead>
<tr>
<th align="left" colspan="3">
Users settings
</th>
</tr>
</thead>

<tr>
<td>

`commafeed.users.allow-registrations`

Whether to let users create accounts for themselves.



Environment variable: `COMMAFEED_USERS_ALLOW_REGISTRATIONS`</td>
<td>

boolean
</td>
<td>

`false`
</td>
</tr>
<tr>
<td>

`commafeed.users.strict-password-policy`

Whether to enable strict password validation (1 uppercase char, 1 lowercase char, 1 digit, 1 special char).



Environment variable: `COMMAFEED_USERS_STRICT_PASSWORD_POLICY`</td>
<td>

boolean
</td>
<td>

`true`
</td>
</tr>
<tr>
<td>

`commafeed.users.create-demo-account`

Whether to create a demo account the first time the app starts.



Environment variable: `COMMAFEED_USERS_CREATE_DEMO_ACCOUNT`</td>
<td>

boolean
</td>
<td>

`false`
</td>
</tr>
<thead>
<tr>
<th align="left" colspan="3">
Websocket settings
</th>
</tr>
</thead>

<tr>
<td>

`commafeed.websocket.enabled`

Enable websocket connection so the server can notify web clients that there are new entries for feeds.



Environment variable: `COMMAFEED_WEBSOCKET_ENABLED`</td>
<td>

boolean
</td>
<td>

`true`
</td>
</tr>
<tr>
<td>

`commafeed.websocket.ping-interval`

Interval at which the client will send a ping message on the websocket to keep the connection alive.



Environment variable: `COMMAFEED_WEBSOCKET_PING_INTERVAL`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`15M`
</td>
</tr>
<tr>
<td>

`commafeed.websocket.tree-reload-interval`

If the websocket connection is disabled or the connection is lost, the client will reload the feed tree at this interval.



Environment variable: `COMMAFEED_WEBSOCKET_TREE_RELOAD_INTERVAL`</td>
<td>

[Duration](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html) [🛈](#duration-note-anchor)
</td>
<td>

`30S`
</td>
</tr>
</tbody>
</table>

<a name="duration-note-anchor"></a>

> [!NOTE]
> ### About the Duration format
> 
> To write duration values, use the standard `java.time.Duration` format.
> See the [Duration#parse()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html#parse(java.lang.CharSequence)) Java API documentation] for more information.
> 
> You can also use a simplified format, starting with a number:
> 
> * If the value is only a number, it represents time in seconds.
> * If the value is a number followed by `ms`, it represents time in milliseconds.
> 
> In other cases, the simplified format is translated to the `java.time.Duration` format for parsing:
> 
> * If the value is a number followed by `h`, `m`, or `s`, it is prefixed with `PT`.
> * If the value is a number followed by `d`, it is prefixed with `P`.
<a name="memory-size-note-anchor"></a>

> [!NOTE]
> ### About the MemorySize format
> 
> A size configuration option recognizes strings in this format (shown as a regular expression): `[0-9]+[KkMmGgTtPpEeZzYy]?`.
> 
> If no suffix is given, assume bytes.
