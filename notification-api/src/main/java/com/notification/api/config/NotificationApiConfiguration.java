package com.notification.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class NotificationApiConfiguration extends Configuration {
    
    @Valid
    @NotNull
    @JsonProperty("database")
    private DataSourceFactory database = new DataSourceFactory();
    
    @JsonProperty("redis")
    private RedisConfig redisConfig = new RedisConfig();
    
    @JsonProperty("jwtSecret")
    private String jwtSecret = "notification-secret-key-2023";
    
    @JsonProperty("apiKeys")
    private List<String> apiKeys = List.of("notif-api-key-001", "notif-api-key-002");
    
    @JsonProperty("rateLimiting")
    private RateLimitingConfig rateLimitingConfig = new RateLimitingConfig();
    
    @JsonProperty("retryConfig")
    private RetryConfig retryConfig = new RetryConfig();

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    public void setRedisConfig(RedisConfig redisConfig) {
        this.redisConfig = redisConfig;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public List<String> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(List<String> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public RateLimitingConfig getRateLimitingConfig() {
        return rateLimitingConfig;
    }

    public void setRateLimitingConfig(RateLimitingConfig rateLimitingConfig) {
        this.rateLimitingConfig = rateLimitingConfig;
    }

    public RetryConfig getRetryConfig() {
        return retryConfig;
    }

    public void setRetryConfig(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
    }

    public static class RedisConfig {
        @JsonProperty("host")
        private String host = "localhost";
        
        @JsonProperty("port")
        private int port = 6379;
        
        @JsonProperty("password")
        private String password = null;
        
        @JsonProperty("timeout")
        private int timeout = 2000;
        
        @JsonProperty("maxConnections")
        private int maxConnections = 20;
        
        @JsonProperty("maxIdle")
        private int maxIdle = 10;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        
        public int getMaxIdle() { return maxIdle; }
        public void setMaxIdle(int maxIdle) { this.maxIdle = maxIdle; }
    }

    public static class RateLimitingConfig {
        @JsonProperty("enabled")
        private boolean enabled = true;
        
        @JsonProperty("requestsPerMinute")
        private int requestsPerMinute = 1000;
        
        @JsonProperty("requestsPerHour")
        private int requestsPerHour = 10000;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
        
        public int getRequestsPerHour() { return requestsPerHour; }
        public void setRequestsPerHour(int requestsPerHour) { this.requestsPerHour = requestsPerHour; }
    }

    public static class RetryConfig {
        @JsonProperty("maxRetries")
        private int maxRetries = 3;
        
        @JsonProperty("initialDelayMs")
        private long initialDelayMs = 1000;
        
        @JsonProperty("maxDelayMs")
        private long maxDelayMs = 30000;
        
        @JsonProperty("backoffMultiplier")
        private double backoffMultiplier = 2.0;

        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        
        public long getInitialDelayMs() { return initialDelayMs; }
        public void setInitialDelayMs(long initialDelayMs) { this.initialDelayMs = initialDelayMs; }
        
        public long getMaxDelayMs() { return maxDelayMs; }
        public void setMaxDelayMs(long maxDelayMs) { this.maxDelayMs = maxDelayMs; }
        
        public double getBackoffMultiplier() { return backoffMultiplier; }
        public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = backoffMultiplier; }
    }
}