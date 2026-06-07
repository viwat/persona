package com.example.persona.pinless.config;

import com.example.persona.pinless.model.dto.PinlessConfigData;
import com.example.persona.pinless.model.entity.PinlessAuthPolicy;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class PinlessCacheConfig implements CachingConfigurer {

    public static final String CACHE_MANAGER = "pinlessCacheManager";
    public static final String CACHE_NAME = "pinlessConfig";
    public static final String CACHE_AUTH_POLICY = "pinlessAuthPolicy";

    private final PinlessSystemProperties systemProperties;

    @Bean(CACHE_MANAGER)
    @ConditionalOnBean(LettuceConnectionFactory.class)
    public CacheManager pinlessCacheManager(LettuceConnectionFactory redisConnectionFactory) {
        String prefix = systemProperties.getCache().resolvedPrefix();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig(defaultSerializer(), prefix))
                .withInitialCacheConfigurations(Map.of(
                        CACHE_NAME, cacheConfig(typedSerializer(PinlessConfigData.class), prefix),
                        CACHE_AUTH_POLICY, cacheConfig(typedSerializer(PinlessAuthPolicy.class), prefix)))
                .build();
    }

    @Bean(CACHE_MANAGER)
    @ConditionalOnBean(name = "!pinlessCacheManager")
    public CacheManager pinlessFallbackCacheManager() {
        return new ConcurrentMapCacheManager(CACHE_NAME, CACHE_AUTH_POLICY);
    }

    private RedisCacheConfiguration cacheConfig(RedisSerializer<?> valueSerializer, String prefix) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(systemProperties.getCache().getTtlMinutes()))
                .computePrefixWith(cacheName -> prefix + cacheName + "::")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();
    }

    private <T> GenericJacksonJsonRedisSerializer typedSerializer(Class<T> type) {
        JacksonObjectReader typedReader = (mapper, bytes, javaType) -> mapper.readValue(bytes, type);
        return GenericJacksonJsonRedisSerializer.builder()
                .customize(b -> b.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))
                .reader(typedReader)
                .writer(JacksonObjectWriter.create())
                .build();
    }

    private GenericJacksonJsonRedisSerializer defaultSerializer() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.example.persona.pinless")
                .build();
        return GenericJacksonJsonRedisSerializer.builder()
                .customize(b -> b.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))
                .enableDefaultTyping(ptv)
                .enableSpringCacheNullValueSupport()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new PinlessCacheErrorHandler();
    }
}
