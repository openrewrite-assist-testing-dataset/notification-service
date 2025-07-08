package com.notification.api.health;

import com.codahale.metrics.health.HealthCheck;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisHealthCheck extends HealthCheck {
    private final JedisPool jedisPool;

    public RedisHealthCheck(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    protected Result check() throws Exception {
        try (Jedis jedis = jedisPool.getResource()) {
            String response = jedis.ping();
            if ("PONG".equals(response)) {
                return Result.healthy("Redis connection is healthy");
            } else {
                return Result.unhealthy("Redis ping returned: " + response);
            }
        } catch (Exception e) {
            return Result.unhealthy("Redis connection failed: " + e.getMessage());
        }
    }
}