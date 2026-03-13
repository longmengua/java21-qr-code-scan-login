package com.example.demo.service;

import com.example.demo.cache.CacheService;
import com.example.demo.enums.RedisKeys;
import com.example.demo.model.LoginType;
import com.example.demo.model.QrLoginState;
import com.example.demo.model.TokenPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrLoginService {
    private final CacheService cacheService;
    private final AuthService authService;

    public String init() {
        QrLoginState state = QrLoginState
                .builder()
                .qrCodeId(UUID.randomUUID().toString())
                .qrCodeImg(UUID.randomUUID().toString())
                .status(QrLoginState.Status.INIT)
                .build();

        cacheService.put(RedisKeys.QR_KEY + state.getQrCodeId(), state, Duration.ofMinutes(5));
        return state.getQrCodeId();
    }

    public TokenPair confirm(String qrCodeId, String userId) {
        // 從快取讀取 QrCode 資訊
        Optional<QrLoginState> state = cacheService.get(RedisKeys.QR_KEY + qrCodeId, QrLoginState.class);

        // 檢查 QrCode 合法性
        if (state.isEmpty() || !state.get().isValidatedQrCode(userId)) {
            throw new SecurityException("Invalid QR");
        }

        // 更新 qr code 狀態，並設置只有 n 分鐘還可以查詢到
        state.get().setStatus(QrLoginState.Status.CONFIRMED);
        cacheService.put(RedisKeys.QR_KEY + qrCodeId, state, Duration.ofMinutes(5));

        // 掃碼成功，建立 SCAN 登入 session
        return authService.generateToken(userId, LoginType.SCAN);
    }

    public Optional<QrLoginState> get(String qrId) {
        return cacheService.get(RedisKeys.QR_KEY + qrId, QrLoginState.class);
    }
}
