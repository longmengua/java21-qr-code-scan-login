package com.example.demo.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "auth.cache", havingValue = "local", matchIfMissing = true)
public class LocalAuthCache implements AuthCache {

    private static class CacheItem {
        Object value;
        long expireAt;
    }

    private final ConcurrentHashMap<String, CacheItem> store = new ConcurrentHashMap<>();

    @Override
    public void put(String key, Object value, Duration ttl) {
        CacheItem item = new CacheItem();
        item.value = value;
        item.expireAt = System.currentTimeMillis() + ttl.toMillis();
        store.put(key, item);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        CacheItem item = store.get(key);
        if (item == null || item.expireAt < System.currentTimeMillis()) {
            store.remove(key);
            return null;
        }
        return (T) item.value;
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }

    @Override
    public boolean exists(String key) {
        return get(key, Object.class) != null;
    }
}

