package com.onlineshop.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/1/11
 */
@Configuration
public class CacheConfig {
    /**
     * CacheManager Bean，Spring Cache 注解使用它
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(cacheConfiguration()) // 默认配置
                                .transactionAware()                 // 支持事务
                                .build();
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        return RedisCacheConfiguration.defaultCacheConfig()
                                      .serializeKeysWith(
                                              RedisSerializationContext.SerializationPair.fromSerializer(
                                                      new StringRedisSerializer()))
                                      .serializeValuesWith(
                                              RedisSerializationContext.SerializationPair.fromSerializer(
                                                      serializer))
                                      .disableCachingNullValues();
    }
}
