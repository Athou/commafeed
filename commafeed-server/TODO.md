TODO
----

MVP:

- cookie duration too short
    - https://github.com/quarkusio/quarkus/issues/42463
    - Rewrite cookie with https://quarkus.io/guides/rest#request-or-response-filters in the mean time

- mvn profile instead of -Dquarkus.datasource.db-kind
- update github actions
    - release after tag
        - new job that downloads all artifacts because we need them all to create the release
- update readme
- update release notes (+ mention h2 migration has been removed, upgrade to last 4.x is required)

Nice to have:

- find a better way to scan rome classes
- remove suppresswarnings "deprecation"
- remove rest assured or use only rest assured
- rename "servlets" since they are now rest endpoints
- warnings hibernate on startup
- OPML encoding is not handled correctly
- remove Timers metrics page

native-image
-------------

- https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Resources/
- https://github.com/rometools/rome/pull/636/files