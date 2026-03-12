package com.example.demo.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LocalMemoryCacheService
 *
 * 本地記憶體快取實作：
 * - 使用 ConcurrentHashMap 儲存資料
 * - 支援 TTL 過期機制
 * - 僅建議用於 local development 或測試環境
 *
 * 啟用條件：
 * cache.type=local
 * 或未設定 cache.type（matchIfMissing = true）
 */
@Service
@ConditionalOnProperty(name = "cache.type", havingValue = "local", matchIfMissing = true)
public class LocalMemoryCacheService implements CacheService {

    private final ObjectMapper objectMapper;

    public LocalMemoryCacheService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 內部快取物件封裝：
     * value     → 真正存的資料
     * expireAt  → 過期時間（timestamp 毫秒）
     */
    private static class CacheObject {
        Object value;
        long expireAt;
    }

    /**
     * 核心儲存容器
     * key   → cache key
     * value → CacheObject (包含值與過期時間)
     */
    private final ConcurrentHashMap<String, CacheObject> store = new ConcurrentHashMap<>();

    /**
     * 放入快取
     *
     * @param key   快取 key
     * @param value 任意物件
     * @param ttl   存活時間
     */
    @Override
    public <T> void put(String key, T value, Duration ttl) {

        long now = System.currentTimeMillis();

        CacheObject obj = new CacheObject();
        obj.value = value;
        obj.expireAt = now + ttl.toMillis();

        store.put(key, obj);
    }

    /**
     * 取得快取（泛型安全版本）
     *
     * @param key  快取 key
     * @param type 期望取得的型別
     *
     * @return Optional<T>
     */
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {

        CacheObject obj = store.get(key);

        if (obj == null) {
            return Optional.empty();
        }

        long now = System.currentTimeMillis();

        // 若已過期，移除並回傳 empty
        if (now > obj.expireAt) {
            store.remove(key);
            return Optional.empty();
        }

        Object value = obj.value;

        // 型別檢查（避免 ClassCastException）
        if (!type.isInstance(value)) {
            throw new IllegalStateException(
                    "Cache type mismatch. Expected: "
                            + type.getName()
                            + ", but was: "
                            + value.getClass().getName()
            );
        }

        return Optional.of(type.cast(value));
    }

    /**
     * 刪除快取
     *
     * @param key 快取 key
     */
    @Override
    public void delete(String key) {
        store.remove(key);
    }

    /**
     * 取得快取所有內容
     */
    @Override
    public Optional<String> getAll(List<String> prefixes) {
        try {
            long now = System.currentTimeMillis();

            // 過濾過期 key 並保留符合 prefix 的 key
            List<Map<String, Object>> result = store.entrySet().stream()
                    .filter(entry -> {
                        CacheObject obj = entry.getValue();
                        if (now > obj.expireAt) {
                            store.remove(entry.getKey()); // 移除過期
                            return false;
                        }
                        return true;
                    })
                    .filter(entry -> {
                        if (prefixes == null || prefixes.isEmpty()) {
                            return true; // 保留全部
                        }
                        String key = entry.getKey();
                        return prefixes.stream().anyMatch(key::startsWith);
                    })
                    .map(entry -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("key", entry.getKey());
                        map.put("value", entry.getValue().value);
                        return map;
                    })
                    .toList();

            return Optional.of(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            return Optional.of("[]");
        }
    }
}