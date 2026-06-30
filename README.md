# CommaFeed

Google Reader inspired self-hosted RSS reader, based on Quarkus and React/TypeScript.

![preview](https://user-images.githubusercontent.com/1256795/184886828-1973f148-58a9-4c6d-9587-ee5e5d3cc2cb.png)

## Features

- 4 different layouts
- Light/Dark theme
- Fully responsive, works great on both mobile and desktop
- Keyboard shortcuts for almost everything
- Support for right-to-left feeds
- Translated in 25+ languages
- Supports thousands of users and millions of feeds
- OPML import/export
- REST API
- Fever-compatible API for native mobile apps
- Can automatically mark articles as read based on user-defined rules
- Push notifications when new articles are published
- Highly customizable with [custom CSS](https://athou.github.io/commafeed/documentation/custom-css) and JavaScript
- [Browser extension](https://github.com/Athou/commafeed-browser-extension)
- Compiles to native code for blazing fast startup and low memory usage
- Supports 4 databases
    - H2 (embedded database)
    - PostgreSQL
    - MySQL
    - MariaDB

## Usage

### Public instance

A free public instance is available at https://www.commafeed.com.

It has no ads, no tracking, and your data is never exploited or sold to third parties. The service is funded entirely through donations.
However, this public instance does have a few limitations compared to self-hosted setups, outlined [here](https://github.com/Athou/commafeed/discussions/1567).

### Docker

Docker is the easiest way to get started with self-hosted CommaFeed.

Docker images are built automatically and are available at https://hub.docker.com/r/athou/commafeed

### Cloud hosting

[PikaPods](https://www.pikapods.com) offers 1-click cloud hosting solutions starting at $1/month with a free $5
welcome credit and officially supports CommaFeed.
PikaPods shares 20% of the revenue back to CommaFeed.

[![PikaPods](https://www.pikapods.com/static/run-button.svg)](https://www.pikapods.com/pods?run=commafeed)

### Download a precompiled package

Go to the [release page](https://github.com/Athou/commafeed/releases) and download the latest version for your operating
system and database of choice.

There are two types of packages:

- The `linux-x86_64`, `linux-aarch_64` and `windows-x86_64` packages are compiled natively and contain an executable that can be run
  directly.
- The `jvm` package is a zip file containing all `.jar` files required to run the application. This package works on all
  platforms but requires a JRE and is started with `java -jar quarkus-run.jar`.

If available for your operating system, the native package is recommended because it has a faster startup time and lower
memory usage.

### Build from sources

    ./mvnw clean package [-P<database> [-Pnative]] [-DskipTests]

- `<database>` can be one of `h2`, `postgresql`, `mysql` or `mariadb`. The default is `h2`.
- `-Pnative` compiles the application to native code. This requires GraalVM to be installed (`GRAALVM_HOME` environment
  variable pointing to a GraalVM installation).
- `-DskipTests` to speed up the build process by skipping tests.

When the build is complete:

- a zip containing all jars required to run the application is located at
  `commafeed-server/target/commafeed-<version>-<database>-jvm.zip`. Extract it and run the application with
  `java -jar quarkus-run.jar`
- if you used the native profile, the executable is located at
  `commafeed-server/target/commafeed-<version>-<database>-<platform>-<arch>-runner[.exe]`

### Distribution packages

- Arch Linux users can use [the CommaFeed package on AUR](https://aur.archlinux.org/pkgbase/commafeed), which builds native binaries with GraalVM for all supported databases.

## Configuration

CommaFeed doesn't require any configuration to run with its embedded database (H2). The database file will be stored in
the `data` directory of the current directory.

To use a different database, you will need to configure the following properties:

- `quarkus.datasource.jdbc.url`
    - e.g. for H2: `jdbc:h2:./data/db;DEFRAG_ALWAYS=TRUE`
    - e.g. for PostgreSQL: `jdbc:postgresql://localhost:5432/commafeed`
    - e.g. for MySQL:
      `jdbc:mysql://localhost/commafeed?autoReconnect=true&failOverReadOnly=false&maxReconnects=20&rewriteBatchedStatements=true&timezone=UTC`
    - e.g. for MariaDB:
      `jdbc:mariadb://localhost/commafeed?autoReconnect=true&failOverReadOnly=false&maxReconnects=20&rewriteBatchedStatements=true&timezone=UTC`
- `quarkus.datasource.username`
- `quarkus.datasource.password`

There are multiple ways to configure CommaFeed:

- a `config/application.properties` [properties](https://en.wikipedia.org/wiki/.properties) file relative to the working
  directory (keys in kebab-case)
- Command line arguments each prefixed with `-D` (keys in kebab-case)
- Environment variables (keys in UPPER_CASE)
- a `.env` file in the working directory (keys in UPPER_CASE)

When in doubt, the properties file is recommended because CommaFeed will be able to warn about invalid properties and typos.

All [CommaFeed settings](https://athou.github.io/commafeed/documentation) are optional and have sensible default values.

When logging in, credentials are stored in an encrypted cookie. The encryption key is randomly generated at startup,
meaning that you will have to log back in after each restart of the application. To prevent this, you can set the
`quarkus.http.auth.session.encryption-key` property to a fixed value (min. 16 characters).
All other Quarkus settings can be found [here](https://quarkus.io/guides/all-config).

When started, the server will listen on http://localhost:8082.


### Troubleshooting feed redirects

When adding a feed, CommaFeed follows HTTP redirects returned by the site until it reaches the final RSS or Atom document.
If the target site sends CommaFeed through the same URL more than once, the add-feed request can fail with a circular redirect error.
This usually means the publisher's feed endpoint, CDN, or anti-bot layer is redirecting server-side clients differently than a browser.
It can happen with journal platforms such as OJS when the advertised feed URL redirects to a journal home page that redirects back to the feed.

To diagnose the problem, first find the final feed URL that the server is trying to reach instead of relying only on the browser address bar.
From the machine running CommaFeed, run `curl -IL <feed-url>` and read the `Location` headers in order.
If the same URL appears repeatedly, or if the chain alternates between `http` and `https`, the feed publisher needs to fix the redirect rules.
Try subscribing to the last URL in the chain that returns `200 OK` with an RSS or Atom content type such as `application/rss+xml`, `application/atom+xml`, or `application/xml`.

Common causes are mixed HTTP/HTTPS canonical URLs, a feed URL missing a required trailing slash, and publisher rules that redirect mobile or non-browser clients.
Some sites also require cookies, consent pages, or a particular `User-Agent` before serving the feed.
CommaFeed cannot safely complete a subscription when a feed is only reachable after a browser-only cookie or JavaScript challenge.
If a browser works but CommaFeed fails, compare it with `curl -IL -A 'CommaFeed' <feed-url>` and then with a normal browser-like user agent.

For OJS feeds, check whether the journal exposes alternate endpoints for issue, announcement, or article feeds and subscribe to the direct XML endpoint when available.
Also verify that the feed URL is public, does not require login, and does not redirect from one locale, journal path, or hostname back to another.
If you run the site, make the feed URL return the feed directly over HTTPS and avoid redirecting feed requests through the HTML journal landing page.
Prefer one canonical hostname for feeds, for example either `example.org` or `www.example.org`, so CommaFeed does not bounce between hosts.
After changing redirects, retest from outside the publisher network because internal DNS or proxy rules can hide the public redirect loop.

When reporting a circular redirect feed-add failure, include the original URL, the redirect chain from `curl -IL`, the CommaFeed version, and whether the same command was run from the CommaFeed host.
Mention any reverse proxy, VPN, corporate proxy, or container network that might change how the publisher sees the request.
If possible, include the final working RSS or Atom URL so maintainers can tell whether the problem is site configuration, network policy, or CommaFeed handling.

### Updates

When CommaFeed is up and running, you can subscribe to [this feed](https://github.com/Athou/commafeed/releases.atom) to be notified of new releases.

### Memory management

The Java Virtual Machine (JVM) is rather greedy by default and will not release unused memory to the
operating system. This is because acquiring memory from the operating system is a relatively expensive operation.
This can be problematic on systems with limited memory.

#### Hard limit (`native` and `jvm` packages)

The JVM can be configured to use a maximum amount of memory with the `-Xmx` parameter.
For example, to limit the JVM to 256MB of memory, use `-Xmx256m`.

#### Dynamic sizing (`jvm` package)

In addition to the previous setting, the JVM can be configured to release unused memory to the operating system with the
following parameters:

    -Xms20m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:-ShrinkHeapInSteps -XX:G1PeriodicGCInterval=10000 -XX:-G1PeriodicGCInvokesConcurrent -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10

See [here](https://docs.oracle.com/en/java/javase/17/gctuning/garbage-first-g1-garbage-collector1.html)
and [here](https://docs.oracle.com/en/java/javase/17/gctuning/factors-affecting-garbage-collection-performance.html) for
more
information.

#### OpenJ9 (`jvm` package)

The [OpenJ9](https://eclipse.dev/openj9/) JVM is a more memory-efficient alternative to the HotSpot JVM, at the cost of
slightly slower throughput.

IBM provides precompiled binaries for OpenJ9
named [Semeru](https://developer.ibm.com/languages/java/semeru-runtimes/downloads/).
This is the JVM used in
the [Docker image](https://github.com/Athou/commafeed/blob/master/commafeed-server/src/main/docker/Dockerfile.jvm).

## Translation

Files for internationalization are
located [here](https://github.com/Athou/commafeed/tree/master/commafeed-client/src/locales).

To add a new language:

- add the new locale to the `locales` array in:
    - `commafeed-client/.linguirc`
    - `commafeed-client/src/i18n.ts`
- run `npm run i18n:extract`
- add translations to the newly created `commafeed-client/src/locales/[locale]/messages.po` file

The name of the locale should be the
two-letters [ISO-639-1 language code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).

## Local development

### Backend

- Open `commafeed-server` in your preferred Java IDE.
    - CommaFeed uses Lombok, you need the Lombok plugin for your IDE.
- run `./mvnw quarkus:dev`

### Frontend

- Open `commafeed-client` in your preferred JavaScript IDE.
- run `npm install`
- run `npm run dev`

The frontend server is now running at http://localhost:8082 and is proxying REST requests to the backend running on
port 8083
