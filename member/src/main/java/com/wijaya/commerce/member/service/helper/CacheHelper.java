package com.wijaya.commerce.member.service.helper;

public interface CacheHelper {
    Object get(String key);

    void set(String key, Object value);

    void set(String key, Object value, long time);

    void delete(String key);
}
