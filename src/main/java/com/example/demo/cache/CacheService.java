package com.example.demo.cache;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface CacheService {

    <T> void put(String key, T value, Duration ttl);

    <T> Optional<T> get(String key, Class<T> type);

    void delete(String key);

    Optional<String> getAll(List<String> prefixes);
}
