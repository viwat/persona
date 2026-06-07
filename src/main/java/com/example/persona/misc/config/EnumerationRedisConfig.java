package com.example.persona.misc.config;

import com.example.persona.misc.model.Enumeration;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class EnumerationRedisConfig {
    @Bean
    public RedisTemplate<String, Enumeration> enumerationRedisTemplate(
            RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Enumeration> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        JacksonJsonRedisSerializer<@NonNull Enumeration> valueSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, Enumeration.class);
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
