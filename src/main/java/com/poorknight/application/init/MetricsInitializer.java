package com.poorknight.application.init;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.github.jjagged.metrics.reporting.StatsDReporter;
import com.poorknight.application.RecipeServiceConfiguration;

import io.dropwizard.setup.Bootstrap;

public class MetricsInitializer {

	public static void initializeApplicationMetrics(final String metricsUrl, final String metricsPort, final Bootstrap<RecipeServiceConfiguration> bootstrap) {
		if (canNotStartMetrics(metricsUrl, metricsPort)) {
			LoggerFactory.getLogger(MetricsInitializer.class).info("Unable to start metrics collection - location or port of metrics repository is not defined correctly");
			return;
		}

		final StatsDReporter metricsReporter = buildMetricsReporter(metricsUrl, metricsPort, bootstrap.getMetricRegistry());
		metricsReporter.start(5, TimeUnit.SECONDS);
		LoggerFactory.getLogger(MetricsInitializer.class).error("Metrics collection started.");
	}

	private static StatsDReporter buildMetricsReporter(final String url, final String port, final MetricRegistry metricRegistry) {
		registerApplicationMetrics(metricRegistry);

		final StatsDReporter statsDReporter = StatsDReporter.forRegistry(metricRegistry) //
				.prefixedWith("recipe-service") //
				.convertDurationsTo(TimeUnit.MILLISECONDS) //
				.convertRatesTo(TimeUnit.SECONDS) //
				.build(url, Integer.parseInt(port));
		return statsDReporter;
	}

	private static void registerApplicationMetrics(final MetricRegistry metricRegistry) {
		metricRegistry.registerAll(new MemoryUsageGaugeSet());
		metricRegistry.registerAll(new GarbageCollectorMetricSet());
	}

	private static boolean canNotStartMetrics(final String metricsCollectorUrl, final String metricsCollectorPort) {
		if (StringUtils.isEmpty(metricsCollectorUrl) || StringUtils.isEmpty(metricsCollectorPort) || !StringUtils.isNumeric(metricsCollectorPort)) {
			return true;
		}
		return false;
	}
}
