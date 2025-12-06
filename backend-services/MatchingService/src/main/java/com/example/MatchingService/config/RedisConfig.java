// java
package com.example.MatchingService.config;

import com.example.kafkaevents.events.DriverDataRedis;
import com.example.kafkaevents.events.RiderDataRedis;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  private ObjectMapper redisObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

    // Include `@class` for polymorphic types; restrict to your base package
    PolymorphicTypeValidator ptv =
        BasicPolymorphicTypeValidator.builder().allowIfSubType("com.example").build();
    mapper.activateDefaultTyping(
        ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    return mapper;
  }

  private GenericJackson2JsonRedisSerializer redisSerializer() {
    return new GenericJackson2JsonRedisSerializer(redisObjectMapper());
  }

  @Bean(name = "driverRedisTemplate")
  public RedisTemplate<String, DriverDataRedis> driverRedisTemplate(
      RedisConnectionFactory factory) {
    RedisTemplate<String, DriverDataRedis> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    GenericJackson2JsonRedisSerializer serializer = redisSerializer();

    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(serializer);

    template.afterPropertiesSet();
    return template;
  }

  @Bean(name = "riderRedisTemplate")
  public RedisTemplate<String, RiderDataRedis> riderRedisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, RiderDataRedis> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    GenericJackson2JsonRedisSerializer serializer = redisSerializer();

    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(serializer);

    template.afterPropertiesSet();
    return template;
  }
}
