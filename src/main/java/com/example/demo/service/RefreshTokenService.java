package com.example.demo.service;

import com.example.demo.cache.AuthCache;
import com.example.demo.model.RefreshToken;
import com.example.demo.response.LoginResponse;
import com.example.demo.security.JwtProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final String RT_KEY = "rt:";

    private final AuthCache cache;
    private final JwtProvider jwtProvider;
    private final SessionCoordinator coordinator;

    public RefreshTokenService(AuthCache cache, JwtProvider jwtProvider, SessionCoordinator coordinator) {
        this.cache = cache;
        this.jwtProvider = jwtProvider;
        this.coordinator = coordinator;
    }

    public LoginResponse refresh(String refreshTokenId, String deviceId) {

        RefreshToken rt = cache.get(RT_KEY + refreshTokenId, RefreshToken.class);
        if (rt == null) {
            throw new SecurityException("Invalid refresh token");
        }

        if (!rt.getDeviceId().equals(deviceId)) {
            coordinator.invalidate(rt.getUserId(), rt.getLoginType());
            throw new SecurityException("Device mismatch");
        }

        if (!coordinator.validate(rt.getUserId(), rt.getLoginType(), rt.getSessionId())) {
            throw new SecurityException("Session expired");
        }

        // Rotation：舊的直接刪
        cache.delete(RT_KEY + refreshTokenId);

        String newSessionId = UUID.randomUUID().toString();
        coordinator.bindSession(rt.getUserId(), rt.getLoginType(), newSessionId);

        String access = jwtProvider.generate(
                rt.getUserId(), rt.getLoginType(), newSessionId
        );

        String newRtId = UUID.randomUUID().toString();
        cache.put(
                RT_KEY + newRtId,
                rt,
                Duration.ofDays(30)
        );

        return new LoginResponse(access, newRtId);
    }
}

