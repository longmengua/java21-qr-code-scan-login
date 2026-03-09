package com.example.demo.service;

import com.example.demo.config.AdminProperties;
import com.example.demo.enums.BizErrorCode;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.model.LoginType;
import com.example.demo.model.TokenPair;
import com.example.demo.model.UserLoginInfo;
import com.example.demo.cache.CacheService;
import com.example.demo.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    // Redis Key 前綴 - Refresh Token
    private static final String RT_KEY = "refresh::token::";
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30); // Refresh Token 過期時間

    // Redis Key 前綴 - 使用者 Session
    private static final String SESSION_KEY = "user:session:";
    private static final Duration SESSION_TTL = Duration.ofDays(7); // Session 過期時間

    // 登入互踢規則映射：key 登入類型會踢掉 value 中的登入類型
    private static final Map<LoginType, Set<LoginType>> IMPACT_MAP = Map.of(
            LoginType.SCAN, Set.of(LoginType.WEB),
            LoginType.APP, Set.of(LoginType.SCAN)
    );

    private final CacheService cacheService;
    private final JwtProvider jwtProvider;
    private final AdminProperties adminProperties;

    /**
     * 使用者註冊
     */
    public TokenPair register(String username, String password, LoginType loginType, String deviceId) {
        String userId = authenticate(username, password);

        if (userId != null) {
            throw new BusinessException(BizErrorCode.USER_ALREADY_REGISTERED);
        }

        // TODO: 建立新使用者到資料庫
        userId = UUID.randomUUID().toString();

        return generateTokenPair(userId, loginType, deviceId);
    }

    /**
     * 使用者登入
     */
    public TokenPair login(String username, String password, LoginType loginType, String deviceId) {
        String userId = authenticate(username, password);

        if (userId == null) {
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        return generateTokenPair(userId, loginType, deviceId);
    }

    /**
     * Refresh Token 更新 (旋轉)
     */
    public TokenPair refresh(String refreshToken, String deviceId) {
        // 先從 cache 取得 loginInfo
        UserLoginInfo loginInfo = cacheService
                .get(RT_KEY + refreshToken, UserLoginInfo.class)
                .orElseThrow(() -> new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL));

        // 驗證 session 與裝置是否匹配
        if (!Objects.equals(loginInfo.getDeviceId(), deviceId) ||
                !isSessionValidBySessionId(loginInfo.getUserId(), loginInfo.getLoginType(), loginInfo.getSessionId())) {
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        // 移除舊 session 與 refresh token
        removeSession(refreshToken);

        // 產生新的 Access Token + Refresh Token
        return generateTokenPair(loginInfo.getUserId(), loginInfo.getLoginType(), loginInfo.getDeviceId());
    }

    /**
     * 使用者登出
     */
    public void logout(String refreshToken) {
        removeSession(refreshToken);
    }

    /**
     * 根據 refreshToken 與 deviceId 檢查 session 是否有效
     */
    public boolean isSessionValid(String refreshToken, String deviceId) {
        UserLoginInfo loginInfo = cacheService.get(RT_KEY + refreshToken, UserLoginInfo.class).orElse(null);
        if (loginInfo == null) return false;
        if (!Objects.equals(loginInfo.getDeviceId(), deviceId)) return false;
        return isSessionValidBySessionId(loginInfo.getUserId(), loginInfo.getLoginType(), loginInfo.getSessionId());
    }

    /**
     * 根據 userId, loginType 與 sessionId 檢查 session 是否有效
     * (主要給 JwtAuthFilter 使用)
     */
    public boolean isSessionValidBySessionId(String userId, LoginType loginType, String sessionId) {
        Optional<String> cachedSession = cacheService.get(buildSessionKey(userId, loginType), String.class);
        return cachedSession.map(s -> s.equals(sessionId)).orElse(false);
    }

    /**
     * 統一帳號驗證：管理員或一般使用者
     */
    private String authenticate(String username, String password) {
        if (isAdmin(username, password)) return "0"; // 管理員固定 userId
        return verifyUser(username, password);
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
     * TODO: 從資料庫驗證一般使用者帳號
     */
    private String verifyUser(String username, String password) {
        return null;
    }

    /**
     * 產生 AccessToken + RefreshToken 並註冊 session
     */
    public TokenPair generateTokenPair(String userId, LoginType loginType, String deviceId) {
        // 產生 sessionId
        String sessionId = UUID.randomUUID().toString();

        // 註冊 session 並處理互踢
        registerSession(userId, loginType, sessionId);

        // 生成 AccessToken
        String accessToken = jwtProvider.generate(userId, loginType, sessionId);

        // 生成 RefreshToken 並存到 cache
        String refreshToken = UUID.randomUUID().toString();
        UserLoginInfo userLoginInfo = UserLoginInfo.builder()
                .tokenId(refreshToken)
                .userId(userId)
                .loginType(loginType)
                .sessionId(sessionId)
                .deviceId(deviceId)
                .build();
        cacheService.put(RT_KEY + refreshToken, userLoginInfo, REFRESH_TOKEN_TTL);

        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * 註冊 session 並處理互踢
     */
    private void registerSession(String userId, LoginType loginType, String sessionId) {
        // 互踢影響的登入類型會被刪掉
        IMPACT_MAP.getOrDefault(loginType, Set.of()).forEach(type -> cacheService.delete(buildSessionKey(userId, type)));
        cacheService.put(buildSessionKey(userId, loginType), sessionId, SESSION_TTL);
    }

    /**
     * 移除 session 與 refresh token
     */
    private void removeSession(String refreshToken) {
        UserLoginInfo loginInfo = cacheService.get(RT_KEY + refreshToken, UserLoginInfo.class).orElse(null);
        if (loginInfo != null) {
            cacheService.delete(buildSessionKey(loginInfo.getUserId(), loginInfo.getLoginType()));
            cacheService.delete(RT_KEY + refreshToken);
        }
    }

    /**
     * 生成 Redis session key
     */
    private String buildSessionKey(String userId, LoginType loginType) {
        return SESSION_KEY + userId + ":" + loginType;
    }
}