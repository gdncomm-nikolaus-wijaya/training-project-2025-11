package com.wijaya.commerce.product.service.helper;

public interface CacheService {
  Object get(String key);
  void set(String key, Object value);
  void set(String key, Object value, long time);
  void delete(String key);
}
