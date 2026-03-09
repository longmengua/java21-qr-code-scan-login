package com.example.demo.controller;

import com.example.demo.model.LoginType;
import com.example.demo.model.QrLoginState;
import com.example.demo.model.TokenPair;
import com.example.demo.service.AuthService;
import com.example.demo.service.QrLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/qr")
public class QrLoginController {

    private final QrLoginService qrLoginService;
    private final AuthService authService;
    private final QrSseController sseController;

    /**
     * Web 產生 QR
     */
    @PostMapping("/init")
    public QrLoginState init() {
        return qrLoginService.init();
    }

    /**
     * App 掃碼確認
     */
    @PostMapping("/confirm")
    public void confirm(
            @RequestParam String qrId,
            @RequestParam String qrToken,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        // TODO: 從 App 已登入 session 取得 userId
        String userId = "USER-001";

        qrLoginService.confirm(qrId, qrToken, userId);

        // 掃碼成功，建立 SCAN 登入 session
        TokenPair token = authService.generateTokenPair(userId, LoginType.SCAN, deviceId);

        // SSE 通知 Web 已登入
        sseController.notifyConfirmed(qrId, token);
    }
}
