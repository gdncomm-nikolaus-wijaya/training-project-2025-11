package com.wijaya.commerce.member.serviceImpl.helper;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.wijaya.commerce.member.service.helper.CacheHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CacheHelperImpl implements CacheHelper {

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
