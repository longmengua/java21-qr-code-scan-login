package com.example.demo.cache;

import java.time.Duration;

public interface AuthCache {

    void put(String key, Object value, Duration ttl);

    <T> T get(String key, Class<T> type);

    void delete(String key);

    boolean exists(String key);
}
