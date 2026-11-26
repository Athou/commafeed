package com.commafeed;

import com.codahale.metrics.jackson3.MetricsModule;

import io.quarkus.jackson.JsonMapperBuilderCustomizer;

import jakarta.inject.Singleton;

import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.TimeUnit;

@Singleton
public class JacksonCustomizer implements JsonMapperBuilderCustomizer {
    @Override
    public void customize(JsonMapper.Builder builder) {
        // read and write instants as milliseconds instead of nanoseconds
        builder.enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .disable(DateTimeFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

        // add support for serializing metrics
        builder.addModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, false));
    }
}
