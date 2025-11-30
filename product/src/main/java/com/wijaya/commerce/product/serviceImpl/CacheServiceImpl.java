package com.wijaya.commerce.product.serviceImpl;

import com.wijaya.commerce.product.service.helper.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public Object get(String key) {
    return this.redisTemplate.opsForValue().get(key);
  }

  @Override
  public void set(String key, Object value) {
    this.redisTemplate.opsForValue().set(key, value);
  }

  @Override
  public void set(String key, Object value, long time) {
    this.redisTemplate.opsForValue().set(key, value, time);
  }

  @Override
  public void delete(String key) {
    this.redisTemplate.delete(key);
  }
}
