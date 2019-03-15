package com.expedia.www.spring.cloud.sleuth.haystack.repoter.configuration;

import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.GRPCAgentProtoClient;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import com.expedia.www.haystack.client.metrics.micrometer.MicrometerMetricsRegistry;
import com.expedia.www.spring.cloud.sleuth.haystack.repoter.reporters.HaystackReporter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ragsingh on 05/03/19.
 */

@Configuration
@ConditionalOnProperty(value = "spring.sleuth.haystack.enabled")
@AutoConfigureBefore(TraceAutoConfiguration.class)
@EnableConfigurationProperties(HaystackSettings.class)
public class HaystackReporterAutoConfiguration {

    final static Logger logger = LoggerFactory.getLogger(HaystackReporterAutoConfiguration.class);

    @Value("${spring.application.name}")
    private String serviceName;

    @Bean
    public String serviceName() {
        return serviceName;
    }

    @Bean
    public HaystackReporter spanReporter(String serviceName, List<Client> clients) {
        return new HaystackReporter(serviceName, clients);
    }

    @Bean
    @ConditionalOnMissingBean
    public List<Client> clients(MetricsRegistry metricsRegistry,
                                HaystackSettings settings) {
        List<Client> clients = new ArrayList<>();

        if (settings.getClient().getGrpc() != null) {
            clients.add(new GRPCAgentProtoClient
                    .Builder(metricsRegistry,
                    settings.getClient().getGrpc().getHost(),
                    settings.getClient().getGrpc().getPort()).build());
        }

        return clients;
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsRegistry metricsRegistry(final ObjectProvider<MeterRegistry> meterRegistryObjectProvider) {
        final MeterRegistry meterRegistry = meterRegistryObjectProvider.getIfAvailable();
        if (meterRegistry != null) {
            return new MicrometerMetricsRegistry(meterRegistry);
        }
        return new NoopMetricsRegistry();
    }
}
