FROM eclipse-temurin:17-jre

EXPOSE 8082

RUN mkdir -p /commafeed/data
VOLUME /commafeed/data

COPY commafeed-server/config.yml.example config.yml
COPY commafeed-server/target/commafeed.jar .

CMD ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "commafeed.jar", "server", "config.yml"]
