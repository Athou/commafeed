package com.commafeed;

import java.time.InstantSource;

import com.codahale.metrics.MetricRegistry;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@Singleton
public class CommaFeedProducers {

	@Produces
	@Singleton
	public InstantSource instantSource() {
		return InstantSource.system();
	}

	@Produces
	@Singleton
	public MetricRegistry metricRegistry() {
		return new MetricRegistry();
	}

}
