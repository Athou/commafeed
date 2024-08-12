# CommaFeed

Google Reader inspired self-hosted RSS reader, based on Quarkus and React/TypeScript.

![preview](https://user-images.githubusercontent.com/1256795/184886828-1973f148-58a9-4c6d-9587-ee5e5d3cc2cb.png)

## Features

- 4 different layouts
- Light/Dark theme
- Fully responsive
- Keyboard shortcuts for almost everything
- Support for right-to-left feeds
- Translated in 25+ languages
- Supports thousands of users and millions of feeds
- OPML import/export
- REST API and a Fever-compatible API for native mobile apps
- [Browser extension](https://github.com/Athou/commafeed-browser-extension)
- Compiles to native code for very fast startup and low memory usage
- Supports 4 databases
    - H2 (embedded database)
    - PostgreSQL
    - MySQL
    - MariaDB

## Deployment

### Docker

Docker is the easiest way to get started with CommaFeed.

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

- The `linux` and `windows` packages are compiled natively and contain an executable that can be run directly.
- The `jvm` package contains a Java `.jar` file that works on all platforms and is started with
  `java -jar quarkus-run.jar`.

If available for your operating system, the native package is recommended because it has a faster startup time and lower
memory usage.

### Build from sources

    ./mvnw clean package -P<database> [-DskipTests] [-Pnative]

- `<database>` can be one of `h2`, `postgresql`, `mysql` or `mariadb`.
- `-DskipTests` is optional but recommended because tests require a Docker environment to run against a real database.
- `-Pnative` compiles the application to native code. This requires GraalVM to be installed (GRAALVM_HOME environment
  variable
  pointing to a GraalVM installation).

### Memory management (`jvm` package only)

The Java Virtual Machine (JVM) is rather greedy by default and will not release unused memory to the
operating system. This is because acquiring memory from the operating system is a relatively expensive operation.
This can be problematic on systems with limited memory.

#### Hard limit

The JVM can be configured to use a maximum amount of memory with the `-Xmx` parameter.
For example, to limit the JVM to 256MB of memory, use `-Xmx256m`.

#### Dynamic sizing

In addition to the previous setting, the JVM can be configured to release unused memory to the operating system with the
following parameters:

    -Xms20m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:-ShrinkHeapInSteps -XX:G1PeriodicGCInterval=10000 -XX:-G1PeriodicGCInvokesConcurrent -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10

See [here](https://docs.oracle.com/en/java/javase/17/gctuning/garbage-first-g1-garbage-collector1.html)
and [here](https://docs.oracle.com/en/java/javase/17/gctuning/factors-affecting-garbage-collection-performance.html) for
more
information.

#### OpenJ9

The [OpenJ9](https://eclipse.dev/openj9/) JVM is a more memory-efficient alternative to the HotSpot JVM, at the cost of
slightly slower throughput.

IBM provides precompiled binaries for OpenJ9
named [Semeru](https://developer.ibm.com/languages/java/semeru-runtimes/downloads/).
This is the JVM used in the [Docker image](https://github.com/Athou/commafeed/blob/master/Dockerfile).

## Configuration

There are multiple ways to configure CommaFeed:

- a properties file in `config/application.properties` (kebab-case)
- Command line arguments prefixed with `-D` (kebab-case)
- Environment variables (UPPER_CASE)
- an .env file in the working directory (UPPER_CASE)

The properties file is recommended because CommaFeed will be able to warn about invalid properties and typos.

CommaFeed only requires 3 properties to be configured:

- `quarkus.datasource.username`
- `quarkus.datasource.password`
- `quarkus.datasource.jdbc-url`
    - e.g. for H2: `jdbc:h2:/commafeed/data/db;DEFRAG_ALWAYS=TRUE`
    - e.g. for PostgreSQL: `jdbc:postgresql://localhost:5432/commafeed`
    - e.g. for MySQL:
      `jdbc:mysql://localhost/commafeed?autoReconnect=true&failOverReadOnly=false&maxReconnects=20&rewriteBatchedStatements=true&timezone=UTC`
    - e.g. for MariaDB:
      `jdbc:mariadb://localhost/commafeed?autoReconnect=true&failOverReadOnly=false&maxReconnects=20&rewriteBatchedStatements=true&timezone=UTC`

All
other [CommaFeed settings](https://github.com/Athou/commafeed/blob/master/commafeed-server/src/main/java/com/commafeed/CommaFeedConfiguration.java)
are optional and have sensible default values.

When started, the server will listen on http://localhost:8082.
The default user is `admin` and the default password is `admin`.

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
- run `mvn quarkus:dev`

### Frontend

- Open `commafeed-client` in your preferred JavaScript IDE.
- run `npm install`
- run `npm run dev`

The frontend server is now running at http://localhost:8082 and is proxying REST requests to the backend running on
port 8083
