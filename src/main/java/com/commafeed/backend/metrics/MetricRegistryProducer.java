package com.commafeed.backend.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.codahale.metrics.MetricRegistry;

@ApplicationScoped
public class MetricRegistryProducer {

	private final MetricRegistry registry = new MetricRegistry();

	@Produces
	public MetricRegistry produceMetricsRegistry() {
		return registry;
	}
}
