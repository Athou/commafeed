TODO
----

- h2 migration service executes too late, datasource has already started
- liquibase starts too late, hibernate prints schema warnings about everything that is missing
- find a better way to scan rome classes
- Cache busting https://quarkus.io/guides/rest#controlling-http-caching-features
- CommaFeedConfiguration/RedisConfiguration
    - comments
    - remove cf.app -> commafeed
    - verify validation
    - verify default values
    - reorganize (nested) if needed (feed, etc)
    - rename and use smart types (Duration, etc)
- @RolesAllowed where needed
- cookie encryption key
- cookie duration too short (Session)
    - see PersistentLoginManager
- saveOrUpdate deprecated
- disable /q endpoint https://quarkus.io/guides/management-interface-reference
- verify openapi.json/redoc
- run IT tests in native mode too
- update dockerfile
- update readme
- remove rest assured or use only rest assured
- performLoginActivities on login
- rename "servlets" since they are now rest endpoints
- warnings hibernate on startup
- OPML encoding is not handled correctly
- log levels for prod, test and dev
- remove Timers metrics page
- dev services for redis/redis configuration from quarkus instead of our own/it test with redis
- quarkus mailer for smtp
- https://quarkus.io/guides/lifecycle#graceful-shutdown

native-image
-------------

- https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Resources/
- https://github.com/rometools/rome/pull/636/files