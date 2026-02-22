package com.example.demo.service;

import com.example.demo.cache.CacheService;
import com.example.demo.config.AdminProperties;
import com.example.demo.enums.BizErrorCode;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.model.LoginType;
import com.example.demo.model.RefreshToken;
import com.example.demo.response.LoginResponse;
import com.example.demo.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * LoginService
 *
 * 負責登入流程：
 * 1. 驗證帳密（admin 優先）
 * 2. 建立 session
 * 3. 產生 AccessToken
 * 4. 建立 RefreshToken
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final JwtProvider jwtProvider;
    private final SessionCoordinator coordinator;
    private final CacheService cacheService;
    private final AdminProperties adminProperties;

    private static final String RT_KEY = "refresh::token::";
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    public LoginResponse login(
            String username,
            String password,
            LoginType loginType,
            String deviceId
    ) {

        // 1️⃣ 基本參數檢查
        validateInput(username, password, loginType, deviceId);

        // 2️⃣ 驗證身份（admin 優先）
        String userId = authenticate(username, password);

        // 3️⃣ 建立 session
        String sessionId = UUID.randomUUID().toString();
        coordinator.bindSession(userId, loginType, sessionId);

        // 4️⃣ 產生 AccessToken
        String accessToken = jwtProvider.generate(userId, loginType, sessionId);

        // 5️⃣ 建立 RefreshToken
        String refreshTokenId = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(refreshTokenId)
                .userId(userId)
                .loginType(loginType)
                .sessionId(sessionId)
                .deviceId(deviceId)
                .build();

        cacheService.put(RT_KEY + refreshTokenId, refreshToken, REFRESH_TOKEN_TTL);

        return new LoginResponse(accessToken, refreshTokenId);
    }

    /**
     * 統一驗證入口
     */
    private String authenticate(String username, String password) {

        // 1️⃣ 先驗證 admin
        if (isAdmin(username, password)) {
            return "0"; // 固定 admin userId
        }

        // 2️⃣ 驗證一般使用者
        return verifyUser(username, password)
                .orElseThrow(() ->
                        new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL)
                );
    }

    /**
     * 驗證管理員（從 yaml 讀取）
     */
    private boolean isAdmin(String username, String password) {

        if (adminProperties.getAccounts() == null ||
                adminProperties.getAccounts().isEmpty()) {
            return false;
        }

        return adminProperties.getAccounts()
                .stream()
                .anyMatch(acc ->
                        acc.getUsername().equals(username) &&
                                acc.getPassword().equals(password)
                );
    }

    /**
     * 驗證一般使用者（未來接 DB）
     */
    private java.util.Optional<String> verifyUser(String username, String password) {
        // TODO: 查資料庫
        return java.util.Optional.empty();
    }

    /**
     * 參數檢查
     */
    private void validateInput(
            String username,
            String password,
            LoginType loginType,
            String deviceId
    ) {
        if (isBlank(username)) {
            log.info("missed username");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        if (isBlank(password)) {
            log.info("missed password");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        if (isBlank(deviceId)) {
            log.info("missed deviceId");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        if (loginType == null) {
            log.info("missed loginType");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        return;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}