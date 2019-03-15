package com.expedia.www.spring.cloud.sleuth.haystack.repoter.configuration;

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

        public GrpcConfiguration getGrpc() {
            return grpc;
        }

        public void setGrpc(GrpcConfiguration grpc) {
            this.grpc = grpc;
        }
    }
    
    public static class GrpcConfiguration {
        private String host;
        private int port;

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
}
