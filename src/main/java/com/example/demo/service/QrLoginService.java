package com.example.demo.service;

import com.example.demo.cache.CacheService;
import com.example.demo.model.QrLoginState;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class QrLoginService {

    private static final String QR_KEY = "qr:";

    private final CacheService cacheService;

    public QrLoginService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public QrLoginState init() {
        QrLoginState state = QrLoginState
                .builder()
                .qrId(UUID.randomUUID().toString())
                .qrToken(UUID.randomUUID().toString())
                .status(QrLoginState.Status.INIT)
                .build();

        cacheService.put(QR_KEY + state.getQrId(), state, Duration.ofMinutes(2));
        return state;
    }

    public void confirm(String qrId, String qrToken, String userId) {
        Optional<QrLoginState> state = cacheService.get(QR_KEY + qrId, QrLoginState.class);
        if (state.isEmpty() || !state.get().getQrToken().equals(qrToken)) {
            throw new SecurityException("Invalid QR");
        }
        state.get().setStatus(QrLoginState.Status.CONFIRMED);
        state.get().setConfirmedUserId(userId);
        cacheService.put(QR_KEY + qrId, state, Duration.ofMinutes(1));
    }

    public Optional<QrLoginState> get(String qrId) {
        return cacheService.get(QR_KEY + qrId, QrLoginState.class);
    }
}
