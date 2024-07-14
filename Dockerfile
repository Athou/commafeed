FROM ibm-semeru-runtimes:open-21-jre

EXPOSE 8082

RUN mkdir -p /commafeed/data
VOLUME /commafeed/data

ENV JAVA_TOOL_OPTIONS -Djava.net.preferIPv4Stack=true -Xtune:virtualized -Xminf0.05 -Xmaxf0.1

COPY commafeed-server/config.yml.example config.yml
COPY commafeed-server/target/commafeed.jar .

CMD ["java", "-jar", "commafeed.jar", "server", "config.yml"]
