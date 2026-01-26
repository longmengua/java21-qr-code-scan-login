package com.example.demo.service;

import com.example.demo.cache.AuthCache;
import com.example.demo.model.LoginType;
import com.example.demo.model.RefreshToken;
import com.example.demo.response.LoginResponse;
import com.example.demo.security.JwtProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class LoginService {

    private final JwtProvider jwtProvider;
    private final SessionCoordinator coordinator;
    private final AuthCache cache;

    private static final String RT_KEY = "rt:";

    public LoginService(
            JwtProvider jwtProvider,
            SessionCoordinator coordinator,
            AuthCache cache
    ) {
        this.jwtProvider = jwtProvider;
        this.coordinator = coordinator;
        this.cache = cache;
    }

    public LoginResponse login(
            String username,
            String password,
            LoginType loginType,
            String deviceId
    ) {
        // 1️⃣ 驗證帳密（這裡假設已通過）
        String userId = verifyUser(username, password);

        // 2️⃣ 建立 session
        String sessionId = UUID.randomUUID().toString();
        coordinator.bindSession(userId, loginType, sessionId);

        // 3️⃣ Access Token
        String accessToken = jwtProvider.generate(userId, loginType, sessionId);

        // 4️⃣ Refresh Token（server-side）
        String refreshTokenId = UUID.randomUUID().toString();
        RefreshToken rt = new RefreshToken(
                refreshTokenId,
                userId,
                loginType,
                sessionId,
                deviceId
        );

        cache.put(RT_KEY + refreshTokenId, rt, Duration.ofDays(30));

        return new LoginResponse(accessToken, refreshTokenId);
    }

    private String verifyUser(String username, String password) {
        // TODO 接 DB / IAM
        if (!"test".equals(username)) {
            throw new SecurityException("Invalid user");
        }
        return "USER-001";
    }
}

