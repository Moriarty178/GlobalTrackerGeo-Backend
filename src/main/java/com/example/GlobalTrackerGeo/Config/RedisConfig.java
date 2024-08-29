package com.example.GlobalTrackerGeo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactoryDriverLocation() {
        return new LettuceConnectionFactory("localhost", 911);
    }

    @Bean(name = "redisTemplateDriverLocation")
    public RedisTemplate<String, Object> redisTemplateDriverLocation() {
        return createRedisTemplate(redisConnectionFactoryDriverLocation());
    }

    //Bean mặc định cho các cáu hình, dịch vụ ngầm
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        return createRedisTemplate(redisConnectionFactoryDriverLocation());
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setDefaultSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        redisTemplate.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        redisTemplate.setValueSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        return redisTemplate;
    }
}
