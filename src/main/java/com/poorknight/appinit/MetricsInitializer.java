package com.poorknight.appinit;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.github.jjagged.metrics.reporting.StatsDReporter;
import com.richo.test.dropwizard.HelloWorldConfiguration;

import io.dropwizard.setup.Bootstrap;

public class MetricsInitializer {

	private final static Logger logger = LoggerFactory.getLogger(MetricsInitializer.class);

	public static void initializeApplicationMetrics(final String metricsUrl, final String metricsPort, final Bootstrap<HelloWorldConfiguration> bootstrap) {
		if (canNotStartMetrics(metricsUrl, metricsPort)) {
			return;
		}

		final StatsDReporter metricsReporter = buildMetricsReporter(metricsUrl, metricsPort, bootstrap.getMetricRegistry());
		metricsReporter.start(5, TimeUnit.SECONDS);
		logger.info("Metrics Collection Started.");
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
