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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.sleuth.haystack")
public class HaystackSettings {
    private boolean enabled = true;
    private ClientConfiguration client = new ClientConfiguration();

    public ClientConfiguration getClient() {
        return client;
    }

    public void setClient(ClientConfiguration client) {
        this.client = client;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public static class ClientConfiguration {
        private GrpcConfiguration grpc;
        private HttpConfiguration http;

        public HttpConfiguration getHttp() { return http; }

        public void setHttp(HttpConfiguration http) { this.http = http; }

        public GrpcConfiguration getGrpc() {
            return grpc;
        }

        public void setGrpc(GrpcConfiguration grpc) {
            this.grpc = grpc;
        }
    }
    
    public static class GrpcConfiguration {
        private String host = "localhost";
        private int port = 34000;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class HttpConfiguration {

        private String endpoint = "http://localhost:80/span";

        public String getEndpoint() { return endpoint; }

        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    }
}
