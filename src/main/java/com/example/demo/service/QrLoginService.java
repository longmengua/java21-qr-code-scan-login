package com.example.demo.service;

import com.example.demo.cache.AuthCache;
import com.example.demo.model.QrLoginState;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class QrLoginService {

    private static final String QR_KEY = "qr:";

    private final AuthCache cache;

    public QrLoginService(AuthCache cache) {
        this.cache = cache;
    }

    public QrLoginState init() {
        QrLoginState state = new QrLoginState();
        state.setQrId(UUID.randomUUID().toString());
        state.setQrToken(UUID.randomUUID().toString());
        state.setStatus(QrLoginState.Status.INIT);

        cache.put(QR_KEY + state.getQrId(), state, Duration.ofMinutes(2));
        return state;
    }

    public void confirm(String qrId, String qrToken, String userId) {
        QrLoginState state = cache.get(QR_KEY + qrId, QrLoginState.class);
        if (state == null || !state.getQrToken().equals(qrToken)) {
            throw new SecurityException("Invalid QR");
        }
        state.setStatus(QrLoginState.Status.CONFIRMED);
        state.setConfirmedUserId(userId);
        cache.put(QR_KEY + qrId, state, Duration.ofMinutes(1));
    }

    public QrLoginState get(String qrId) {
        return cache.get(QR_KEY + qrId, QrLoginState.class);
    }
}
