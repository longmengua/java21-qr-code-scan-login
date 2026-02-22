package com.example.demo.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * RedisCacheService
 *
 * Redis 實作的 CacheService
 * - 使用 Spring Data Redis 的 RedisTemplate
 * - 支援存任意物件
 * - TTL 透過 Redis 自動管理
 * - 啟用條件：cache.type=redis
 */
@Service
@ConditionalOnProperty(name = "cache.type", havingValue = "redis")
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    /**
     * RedisTemplate
     * - Key 為 String
     * - Value 為 Object (可存任何序列化物件)
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 將值放入 Redis
     *
     * @param key   快取 key
     * @param value 任意物件
     * @param ttl   存活時間
     */
    @Override
    public <T> void put(String key, T value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 從 Redis 取得值（泛型安全）
     *
     * @param key  快取 key
     * @param type 預期型別
     * @return Optional<T>，不存在或過期時為 empty
     */
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return Optional.empty();
        }

        // 型別檢查，避免 ClassCastException
        if (!type.isInstance(value)) {
            throw new IllegalStateException(
                    "Cache type mismatch. Expected: " + type.getName() +
                            ", but was: " + value.getClass().getName()
            );
        }

        return Optional.of(type.cast(value));
    }

    /**
     * 從 Redis 刪除 key
     *
     * @param key 快取 key
     */
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}