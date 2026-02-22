package com.example.demo.controller;

import com.example.demo.model.LoginType;
import com.example.demo.model.QrLoginState;
import com.example.demo.response.LoginResponse;
import com.example.demo.service.LoginService;
import com.example.demo.service.QrLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/qr")
public class QrLoginController {

    private final QrLoginService qrLoginService;
    private final LoginService loginService;
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
        String userId = "USER-001"; // App 已登入的 user

        qrLoginService.confirm(qrId, qrToken, userId);

        // 掃碼成功 = SCAN 登入
        LoginResponse token = loginService.login(
                userId,
                "N/A",
                LoginType.SCAN,
                deviceId
        );

        // SSE 通知 Web
        sseController.notifyConfirmed(qrId, token);
    }
}
