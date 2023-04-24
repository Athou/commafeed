FROM openjdk:17-alpine

RUN mkdir -p /commafeed/data
VOLUME /commafeed/data

ENV CF_SESSION_PATH=/commafeed/data/sessions

COPY commafeed-server/target/commafeed.jar .
COPY commafeed-server/config.yml.example config.yml

EXPOSE 8082
CMD ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "commafeed.jar", "server", "config.yml"]
