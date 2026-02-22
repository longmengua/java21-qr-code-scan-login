package com.example.demo.service;

import com.example.demo.cache.CacheService;
import com.example.demo.model.LoginType;
import com.example.demo.model.UserSessionIndex;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class SessionCoordinator {

    private static final String USER_SESSION_KEY = "user:sessions:";

    private final CacheService cacheService;

    // 互踢矩陣
    private static final Map<LoginType, Set<LoginType>> IMPACT_MAP = Map.of(
            LoginType.SCAN, Set.of(LoginType.WEB),
            LoginType.APP, Set.of(LoginType.SCAN)
    );

    public SessionCoordinator(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void bindSession(String userId, LoginType type, String sessionId) {
        UserSessionIndex index = getIndex(userId);

        // 先踢人
        invalidateImpacted(userId, type, index);

        index.bind(type, sessionId);
        cacheService.put(USER_SESSION_KEY + userId, index, Duration.ofDays(7));
    }

    public boolean isValidated(String userId, LoginType type, String sessionId) {
        UserSessionIndex index = getIndex(userId);
        return !sessionId.equals(index.get(type));
    }

    public void invalidate(String userId, LoginType type) {
        UserSessionIndex index = getIndex(userId);
        index.remove(type);
        cacheService.put(USER_SESSION_KEY + userId, index, Duration.ofDays(7));
    }

    private void invalidateImpacted(String userId, LoginType loginType, UserSessionIndex index) {
        IMPACT_MAP.getOrDefault(loginType, Set.of())
                .forEach(index::remove);
    }

    private UserSessionIndex getIndex(String userId) {
        Optional<UserSessionIndex> optionalUserSessionIndex = cacheService.get(USER_SESSION_KEY + userId, UserSessionIndex.class);
        return optionalUserSessionIndex.orElseGet(UserSessionIndex::new);
    }
}
