/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.spring.cloud.sleuth.haystack.reporter.configuration;

import com.expedia.www.haystack.client.dispatchers.clients.Client;
import com.expedia.www.haystack.client.dispatchers.clients.GRPCAgentProtoClient;
import com.expedia.www.haystack.client.dispatchers.clients.HttpCollectorClient;
import com.expedia.www.haystack.client.dispatchers.clients.HttpCollectorProtoClient;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import com.expedia.www.haystack.client.metrics.micrometer.MicrometerMetricsRegistry;
import com.expedia.www.spring.cloud.sleuth.haystack.reporter.reporters.HaystackReporter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.Map;
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

        if (settings.getClient().getHttp() != null) {

            final Map<String, String> headers = new HashMap<>();
            headers.put("client-id", serviceName);

            clients.add(new HttpCollectorProtoClient(settings.getClient().getHttp().getEndpoint(), headers));
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
