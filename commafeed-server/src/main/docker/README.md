# CommaFeed

Official docker images for https://github.com/Athou/commafeed/

## Quickstart

Start CommaFeed with a H2 embedded database. Then login as `admin/admin` on http://localhost:8082/

### docker

`docker run --name commafeed --detach --publish 8082:8082 --restart unless-stopped --volume /path/to/commafeed/db:/commafeed/data --memory 256M athou/commafeed:latest-h2`

### docker-compose

```
services:
  commafeed:
    image: athou/commafeed:latest-h2
    restart: unless-stopped
    volumes:
      - /path/to/commafeed/db:/commafeed/data
    deploy:
      resources:
        limits:
          memory: 256M
    ports:
      - 8082:8082
```

## Advanced

While using the H2 embedded database is perfectly fine for small instances, you may want to have more control over the
database. Here's an example that uses postgresql (note image tag change from `latest-h2` to `latest-postgresql`):

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
      - /path/to/commafeed/db:/var/lib/postgresql/data
```

## Configuration

All [CommaFeed settings](https://github.com/Athou/commafeed/blob/master/commafeed-server/doc/commafeed.adoc) are
optional and have sensible default values.

Settings are overrideable with environment variables. For instance, `commafeed.feed-refresh.interval-empirical` can be
set with the `COMMAFEED_FEED_REFRESH_INTERVAL_EMPIRICAL` variable.

When logging in, credentials are stored in an encrypted cookie. The encryption key is randomly generated at startup,
meaning that you will have to log back in after each restart of the application. To prevent this, you can set the
`QUARKUS_HTTP_AUTH_SESSION_ENCRYPTION_KEY` variable to a fixed value (min. 16 characters).

## Docker tags

Tags are of the form `<version>-<database>[-jvm]` where:

- `<version>` is either:
    - a specific CommaFeed version (e.g. `5.0.0`)
    - `latest` (always points to the latest version)
    - `master` (always points to the latest git commit)
- `<database>` is the database to use (`h2`, `postgresql`, `mysql` or `mariadb`)
- `-jvm` is optional and indicates that CommaFeed is running on a JVM, and not compiled natively. This image supports
  the arm64 platform which is not yet supported by the native image.