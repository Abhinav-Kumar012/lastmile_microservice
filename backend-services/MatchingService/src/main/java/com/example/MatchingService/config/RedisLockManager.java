package com.example.MatchingService.config;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLockManager {
  private final StringRedisTemplate redisTemplate;

  public RedisLockManager(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public boolean tryAcquireLock(String lockKey, String lockValue, Duration ttl) {
    Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, ttl);
    return Boolean.TRUE.equals(success);
  }

  public void releaseLock(String lockKey, String lockValue) {
    String currentValue = redisTemplate.opsForValue().get(lockKey);
    if (lockValue.equals(currentValue)) {
      redisTemplate.delete(lockKey);
    }
  }
}
