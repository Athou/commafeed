# CommaFeed

Official docker images for https://github.com/Athou/commafeed/

## Quickstart

Start CommaFeed with a H2 embedded database. Then login as `admin/admin` on http://localhost:8082/

### docker

`docker run --name commafeed --detach --publish 8082:8082 --restart unless-stopped --volume /path/to/commafeed/data:/commafeed/data --memory 256M athou/commafeed:latest-h2`

### docker-compose

```
services:
  commafeed:
    image: athou/commafeed:latest-h2
    restart: unless-stopped
    volumes:
      - ./data:/commafeed/data
    deploy:
      resources:
        limits:
          memory: 256M
    ports:
      - 8082:8082
```

## Advanced

While using the H2 embedded database is perfectly fine for small instances, you may want to have more control over the
database. Here's an example that uses PostgreSQL (note the image tag change from `latest-h2` to `latest-postgresql`):

```
services:
  commafeed:
    image: athou/commafeed:latest-postgresql
    restart: unless-stopped
    environment:
      - QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgresql:5432/commafeed
      - QUARKUS_DATASOURCE_USERNAME=commafeed
      - QUARKUS_DATASOURCE_PASSWORD=commafeed
    deploy:
      resources:
        limits:
          memory: 256M
    ports:
      - 8082:8082

  postgresql:
    image: postgres:latest
    restart: unless-stopped
    environment:
      POSTGRES_USER: commafeed
      POSTGRES_PASSWORD: commafeed
      POSTGRES_DB: commafeed
    volumes:
      - ./data:/var/lib/postgresql
```

CommaFeed also supports:

- MySQL:
  `QUARKUS_DATASOURCE_JDBC_URL=jdbc:mysql://localhost/commafeed?autoReconnect=true&failOverReadOnly=false&maxReconnects=20&rewriteBatchedStatements=true&timezone=UTC`
- MariaDB:
  `QUARKUS_DATASOURCE_JDBC_URL=jdbc:mariadb://localhost/commafeed?autoReconnect=true&failOverReadOnly=false&maxReconnects=20&rewriteBatchedStatements=true&timezone=UTC`

## Configuration

All [CommaFeed settings](https://athou.github.io/commafeed/documentation) are
optional and have sensible default values.

Settings are overrideable with environment variables. For instance, `commafeed.feed-refresh.interval-empirical` can be
set with the `COMMAFEED_FEED_REFRESH_INTERVAL_EMPIRICAL` variable.

When logging in, credentials are stored in an encrypted cookie. The encryption key is randomly generated at startup,
meaning that you will have to log back in after each restart of the application. To prevent this, you can set the
`QUARKUS_HTTP_AUTH_SESSION_ENCRYPTION_KEY` variable to a fixed value (min. 16 characters).
All other Quarkus settings can be found [here](https://quarkus.io/guides/all-config).

### Updates

When CommaFeed is up and running, you can subscribe to [this feed](https://github.com/Athou/commafeed/releases.atom) to be notified of new releases.

## Docker tags

Tags are of the form `<version>-<database>[-jvm]` where:

- `<version>` is either:
    - a specific CommaFeed version (e.g. `5.0.0`)
    - `latest` (always points to the latest version)
    - `master` (always points to the latest git commit)
- `<database>` is the database to use (`h2`, `postgresql`, `mysql` or `mariadb`)
- `-jvm` is optional and indicates that CommaFeed is running on a JVM, and not compiled natively.
