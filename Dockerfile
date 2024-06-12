FROM eclipse-temurin:17.0.11_9-jre

EXPOSE 8082

RUN mkdir -p /commafeed/data
VOLUME /commafeed/data

COPY commafeed-server/config.yml.example config.yml
COPY commafeed-server/target/commafeed.jar .

ENV JAVA_TOOL_OPTIONS -Djava.net.preferIPv4Stack=true -Xms20m -XX:+UseG1GC -XX:-ShrinkHeapInSteps -XX:G1PeriodicGCInterval=10000 -XX:-G1PeriodicGCInvokesConcurrent -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10
CMD ["java", "-jar", "commafeed.jar", "server", "config.yml"]
