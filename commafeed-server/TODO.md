TODO
----

MVP:

- quarkus mailer for smtp
    - https://quarkus.io/guides/mailer
- cookie duration too short
    - https://github.com/quarkusio/quarkus/issues/42463

- mvn profile instead of -Dquarkus.datasource.db-kind
- update dockerfile
    - release after tag (new job that downloads all artifacts because we need them all to create the release)
- update github actions (build and copy outside target each database artifact)
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