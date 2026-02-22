package com.example.demo.service;

import com.example.demo.cache.CacheService;
import com.example.demo.model.RefreshToken;
import com.example.demo.response.LoginResponse;
import com.example.demo.security.JwtProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final String RT_KEY = "rt:";

    private final CacheService cacheService;
    private final JwtProvider jwtProvider;
    private final SessionCoordinator coordinator;

    public RefreshTokenService(CacheService cacheService, JwtProvider jwtProvider, SessionCoordinator coordinator) {
        this.cacheService = cacheService;
        this.jwtProvider = jwtProvider;
        this.coordinator = coordinator;
    }

    public LoginResponse refresh(String refreshTokenId, String deviceId) {

        Optional<RefreshToken> optionalRefreshToken = cacheService.get(RT_KEY + refreshTokenId, RefreshToken.class);
        if (optionalRefreshToken.isEmpty()) {
            throw new SecurityException("Invalid refresh token");
        }

        RefreshToken rt = optionalRefreshToken.get();

        if (!rt.getDeviceId().equals(deviceId)) {
            coordinator.invalidate(rt.getUserId(), rt.getLoginType());
            throw new SecurityException("Device mismatch");
        }

        if (coordinator.isValidated(rt.getUserId(), rt.getLoginType(), rt.getSessionId())) {
            throw new SecurityException("Session expired");
        }

        // Rotation：舊的直接刪
        cacheService.delete(RT_KEY + refreshTokenId);

        String newSessionId = UUID.randomUUID().toString();
        coordinator.bindSession(rt.getUserId(), rt.getLoginType(), newSessionId);

        String access = jwtProvider.generate(
                rt.getUserId(), rt.getLoginType(), newSessionId
        );

        String newRtId = UUID.randomUUID().toString();
        cacheService.put(
                RT_KEY + newRtId,
                rt,
                Duration.ofDays(30)
        );

        return new LoginResponse(access, newRtId);
    }
}

