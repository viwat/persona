package com.example.persona.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.api.StatefulConnection;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${spring.data.redis.timeout:3000ms}")
    private Duration timeout;

    @Value("${spring.data.redis.lettuce.pool.max-active:8}")
    private int maxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:2}")
    private int minIdle;

    @Value("${spring.data.redis.lettuce.pool.max-wait:-1ms}")
    private Duration maxWait;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(host);
        serverConfig.setPort(port);

        if (StringUtils.hasText(password)) {
            serverConfig.setPassword(password);
        }

        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWait(maxWait);
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestWhileIdle(true);

        LettuceClientConfiguration clientConfig;
        if (sslEnabled) {
            clientConfig = LettucePoolingClientConfiguration.builder()
                    .commandTimeout(timeout)
                    .poolConfig(poolConfig)
                    .clientOptions(ClientOptions.builder()
                            .socketOptions(SocketOptions.builder()
                                    .connectTimeout(timeout)
                                    .build())
                            .build())
                    .useSsl()
                    .and()
                    .build();
        } else {
            clientConfig = LettucePoolingClientConfiguration.builder()
                    .commandTimeout(timeout)
                    .poolConfig(poolConfig)
                    .clientOptions(ClientOptions.builder()
                            .socketOptions(SocketOptions.builder()
                                    .connectTimeout(timeout)
                                    .build())
                            .build())
                    .build();
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, clientConfig);
        factory.afterPropertiesSet();

        try {
            factory.getConnection().ping();
            log.info("Redis connection initialized — host={} port={} ssl={}", host, port, sslEnabled);
        } catch (Exception e) {
            log.error("Failed to connect to Redis — host={} port={}: {}", host, port, e.getMessage());
            // Don't throw — runtime failures handled by CacheErrorHandler and circuit breaker
        }

        return factory;
    }

    @Bean
    @Primary
    @ConditionalOnBean(LettuceConnectionFactory.class)
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}
