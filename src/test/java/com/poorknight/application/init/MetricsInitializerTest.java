package com.poorknight.application.init;

import com.codahale.metrics.MetricRegistry;
import com.github.jjagged.metrics.reporting.StatsDReporter;
import com.github.jjagged.metrics.reporting.statsd.StatsD;
import com.poorknight.application.RecipeServiceConfiguration;
import io.dropwizard.setup.Bootstrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StatsDReporter.class, StatsD.class })
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class MetricsInitializerTest {

	private static final String IP_ADDRESS = "192.168.99.100";
	@Mock
	private Bootstrap<RecipeServiceConfiguration> bootstrap;

	@Mock
	private MetricRegistry metricRegistry;

	@Mock
	private StatsDReporter.Builder builder;

	@Mock
	private StatsDReporter reporter;

	@Mock
	private StatsD statsD;

	@Test
	public void metricsAreInitialized_WhenPassedALocationAndPortForMetricsCollector() throws Exception {
		PowerMockito.mockStatic(StatsDReporter.class);
		PowerMockito.mock(StatsD.class);
		PowerMockito.whenNew(StatsD.class).withAnyArguments().thenReturn(statsD);

		when(bootstrap.getMetricRegistry()).thenReturn(metricRegistry);
		when(StatsDReporter.forRegistry(metricRegistry)).thenReturn(builder);

		when(builder.build(IP_ADDRESS, 8125)).thenReturn(reporter);

		when(builder.convertDurationsTo(Mockito.anyObject())).thenReturn(builder);
		when(builder.convertRatesTo(Mockito.anyObject())).thenReturn(builder);
		when(builder.prefixedWith(Mockito.anyString())).thenReturn(builder);

		MetricsInitializer.initializeApplicationMetrics(IP_ADDRESS, "8125", bootstrap);

		verify(reporter).start(5, TimeUnit.SECONDS);
	}

	@Test
	public void metricsAreNOTInitialized_WhenPassedNullLocation() throws Exception {
		MetricsInitializer.initializeApplicationMetrics(null, "8125", bootstrap);
		verifyZeroInteractions(bootstrap);
	}

	@Test
	public void metricsAreNOTInitialized_WhenPassedNullPort() throws Exception {
		MetricsInitializer.initializeApplicationMetrics(IP_ADDRESS, null, bootstrap);
		verifyZeroInteractions(bootstrap);
	}

	@Test
	public void metricsAreNOTInitialized_WhenPassedEmptyLocation() throws Exception {
		MetricsInitializer.initializeApplicationMetrics("", "8125", bootstrap);
		verifyZeroInteractions(bootstrap);
	}

	@Test
	public void metricsAreNOTInitialized_WhenPassedEmptyPort() throws Exception {
		MetricsInitializer.initializeApplicationMetrics(IP_ADDRESS, "", bootstrap);
		verifyZeroInteractions(bootstrap);
	}

	@Test
	public void metricsAreNOTInitialized_WhenPassedNonIntegerParsablePort() throws Exception {
		MetricsInitializer.initializeApplicationMetrics(IP_ADDRESS, "abc", bootstrap);
		verifyZeroInteractions(bootstrap);
	}
}
