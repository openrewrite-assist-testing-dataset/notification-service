package com.notification.api;

import com.notification.api.config.NotificationApiConfiguration;
import com.notification.api.health.DatabaseHealthCheck;
import com.notification.api.health.RedisHealthCheck;
import com.notification.api.resources.NotificationResource;
import com.notification.api.resources.BatchResource;
import com.notification.api.auth.JwtAuthFilter;
import com.notification.api.auth.ApiKeyAuthFilter;
import com.notification.api.auth.NotificationPrincipal;
import com.notification.api.db.NotificationDAO;
import io.dropwizard.core.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import jakarta.ws.rs.container.ContainerRequestFilter;
import java.util.List;

public class NotificationApiApplication extends Application<NotificationApiConfiguration> {
    
    public static void main(String[] args) throws Exception {
        new NotificationApiApplication().run(args);
    }

    @Override
    public String getName() {
        return "notification-api";
    }

    @Override
    public void initialize(Bootstrap<NotificationApiConfiguration> bootstrap) {
        // Legacy initialization - could use newer bootstrap patterns
    }

    @Override
    public void run(NotificationApiConfiguration configuration, Environment environment) {
        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");
        
        // Configure Redis connection
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(configuration.getRedisConfig().getMaxConnections());
        poolConfig.setMaxIdle(configuration.getRedisConfig().getMaxIdle());
        
        final JedisPool jedisPool = new JedisPool(poolConfig, 
            configuration.getRedisConfig().getHost(), 
            configuration.getRedisConfig().getPort(),
            configuration.getRedisConfig().getTimeout(),
            configuration.getRedisConfig().getPassword());
        
        final NotificationDAO notificationDAO = jdbi.onDemand(NotificationDAO.class);
        
        // Setup authentication chain using deprecated patterns
        final ContainerRequestFilter jwtFilter = new JwtAuthFilter.Builder<NotificationPrincipal>()
            .setAuthenticator(new com.notification.api.auth.JwtAuthenticator(configuration.getJwtSecret()))
            .setPrefix("Bearer")
            .buildAuthFilter();
            
        final ContainerRequestFilter apiKeyFilter = new ApiKeyAuthFilter.Builder<NotificationPrincipal>()
            .setAuthenticator(new com.notification.api.auth.ApiKeyAuthenticator(configuration.getApiKeys()))
            .setPrefix("ApiKey")
            .buildAuthFilter();
            
        final ChainedAuthFilter chainedAuthFilter = new ChainedAuthFilter(
            List.of(jwtFilter, apiKeyFilter));
            
        environment.jersey().register(new AuthDynamicFeature(chainedAuthFilter));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<NotificationPrincipal>(NotificationPrincipal.class));
        
        // Register resources
        environment.jersey().register(new NotificationResource(notificationDAO, jedisPool));
        environment.jersey().register(new BatchResource(notificationDAO, jedisPool));
        
        // Register health checks
        environment.healthChecks().register("database", new DatabaseHealthCheck(jdbi));
        environment.healthChecks().register("redis", new RedisHealthCheck(jedisPool));
        
        // Register shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            jedisPool.close();
        }));
    }
}