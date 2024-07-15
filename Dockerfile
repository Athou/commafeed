FROM ibm-semeru-runtimes:open-21-jre

EXPOSE 8082

RUN mkdir -p /commafeed/data
VOLUME /commafeed/data

RUN apt update && apt install -y wait-for-it && apt clean

ENV JAVA_TOOL_OPTIONS -Djava.net.preferIPv4Stack=true -Xtune:virtualized -Xminf0.05 -Xmaxf0.1

COPY commafeed-server/config.docker-warmup.yml .
COPY commafeed-server/config.yml.example config.yml
COPY commafeed-server/target/commafeed.jar .

# build openj9 shared classes cache to improve startup time
RUN sh -c 'java -Xshareclasses -jar commafeed.jar server config.docker-warmup.yml &' ; wait-for-it -t 120 localhost:8088 -- pkill java ; rm -rf config.warmup.yml

CMD ["java", "-Xshareclasses", "-jar", "commafeed.jar", "server", "config.yml"]
