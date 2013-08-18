package com.commafeed.backend.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

@ApplicationScoped
public class MetricRegistryProducer {

	private final MetricRegistry registry = new MetricRegistry();

	@Produces
	public MetricRegistry produceMetricsRegistry() {
		final JmxReporter reporter = JmxReporter.forRegistry(registry).build();
		reporter.start();
		return registry;
	}

}
