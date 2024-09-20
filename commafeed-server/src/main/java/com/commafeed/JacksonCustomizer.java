package com.commafeed;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class JacksonCustomizer implements ObjectMapperCustomizer {
	@Override
	public void customize(ObjectMapper objectMapper) {
		objectMapper.registerModule(new JavaTimeModule());

		// read and write instants as milliseconds instead of nanoseconds
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
				.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
				.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

		// add support for serializing metrics
		objectMapper.registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, false));
	}
}
