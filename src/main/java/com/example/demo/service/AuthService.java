package com.example.demo.service;

import com.example.demo.config.AdminProperties;
import com.example.demo.enums.BizErrorCode;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.model.LoginType;
import com.example.demo.model.TokenPair;
import com.example.demo.cache.CacheService;
import com.example.demo.component.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration SESSION_TTL = Duration.ofDays(7);
    private static final String SESSION_KEY_PREFIX = "user:session:";
    private static final String IMPACT_KEY_PREFIX = "impact:";

    private final CacheService cacheService;
    private final JwtProvider jwtProvider;
    private final AdminProperties adminProperties;

    /**
     * 使用者註冊
     */
    public TokenPair register(String username, String password, LoginType loginType, String deviceId) {
        String userId = verifyUser(username, password);

        if (userId != null) {
            throw new BusinessException(BizErrorCode.USER_ALREADY_REGISTERED);
        }

        // TODO: 建立新使用者到資料庫
        userId = UUID.randomUUID().toString();

        return generateToken(userId, loginType, deviceId);
    }

    /**
     * 使用者登入
     */
    public TokenPair login(String username, String password, LoginType loginType, String deviceId) {
        String userId = verifyUser(username, password);

        if (userId == null) {
            // 檢查是否為管理員
            if (isAdmin(username, password)) {
                userId = "0"; // 管理員固定 userId
            } else {
                throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
            }
        }

        return generateToken(userId, loginType, deviceId);
    }

    /**
     * 使用者登出
     */
    public void logout(String userId, LoginType loginType) {
        cacheService.delete(buildSessionKey(userId, loginType));
        cacheService.delete(buildImpactKey(userId, loginType));
    }

    /**
     * 驗證 session 是否有效
     */
    public boolean isSessionValidBySessionId(String userId, LoginType loginType, String sessionId) {
        Optional<String> optional = cacheService.get(buildSessionKey(userId, loginType), String.class);

        return optional.isPresent() && optional.get().equals(sessionId);
    }

    /**
     * 產生 AccessToken 並註冊 session
     */
    public TokenPair generateToken(String userId, LoginType loginType, String deviceId) {
        String sessionId = UUID.randomUUID().toString();
        registerSession(userId, loginType, sessionId);

        String token = jwtProvider.generate(userId, loginType, sessionId);
        return new TokenPair("Bearer " + token);
    }

    /**
     * 註冊 session 並處理互踢（IMPACT_MAP 存 cache）
     */
    private void registerSession(String userId, LoginType loginType, String sessionId) {
        Set<LoginType> impactedTypes = getImpactedLoginTypes(loginType);

        for (LoginType type : impactedTypes) {
            cacheService.get(buildImpactKey(userId, type), String.class)
                    .ifPresent(impactedSession -> cacheService.delete(buildSessionKey(userId, type)));
        }

        cacheService.put(buildSessionKey(userId, loginType), sessionId, SESSION_TTL);
        cacheService.put(buildImpactKey(userId, loginType), sessionId, SESSION_TTL);
    }

    /**
     * 互踢規則
     */
    private Set<LoginType> getImpactedLoginTypes(LoginType loginType) {
        return switch (loginType) {
            case SCAN -> Set.of(LoginType.WEB);
            case APP -> Set.of(LoginType.SCAN);
            default -> Set.of();
        };
    }

    private String buildSessionKey(String userId, LoginType loginType) {
        return SESSION_KEY_PREFIX + userId + ":" + loginType;
    }

    private String buildImpactKey(String userId, LoginType loginType) {
        return IMPACT_KEY_PREFIX + userId + ":" + loginType;
    }

    /**
     * 驗證是否為管理員帳號
     */
    private boolean isAdmin(String username, String password) {
        if (adminProperties.getAccounts() == null || adminProperties.getAccounts().isEmpty()) return false;
        return adminProperties.getAccounts().stream()
                .anyMatch(acc -> acc.getUsername().equals(username) && acc.getPassword().equals(password));
    }

    /**
     * TODO: 從資料庫驗證一般使用者帳號，返回 userId
     */
    private String verifyUser(String username, String password) {
        return null;
    }
}