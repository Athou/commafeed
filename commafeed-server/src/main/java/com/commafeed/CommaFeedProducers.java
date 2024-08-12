package com.commafeed;

import com.codahale.metrics.MetricRegistry;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@Singleton
public class CommaFeedProducers {

	@Produces
	@Singleton
	public MetricRegistry metricRegistry() {
		return new MetricRegistry();
	}
}
