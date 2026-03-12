package com.example.demo.controller;

import com.example.demo.model.LoginType;
import com.example.demo.model.QrLoginState;
import com.example.demo.model.TokenPair;
import com.example.demo.service.AuthService;
import com.example.demo.service.QrLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/qr")
public class QrLoginController {

    private final QrLoginService qrLoginService;
    private final AuthService authService;

    /**
     * SSE emitter pool
     */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Web 產生 QR
     */
    @PostMapping("/init")
    public QrLoginState init() {
        return qrLoginService.init();
    }

    /**
     * Web 訂閱 QR 登入結果
     */
    @GetMapping("/subscribe/{qrId}")
    public SseEmitter subscribe(@PathVariable String qrId) {
        SseEmitter emitter = new SseEmitter(120000L); // 2 min timeout
        emitters.put(qrId, emitter);

        emitter.onCompletion(() -> emitters.remove(qrId));
        emitter.onTimeout(() -> emitters.remove(qrId));
        emitter.onError(e -> emitters.remove(qrId));

        return emitter;
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
        TokenPair token = authService.generateToken(userId, LoginType.SCAN, deviceId);

        // SSE 通知 Web
        SseEmitter emitter = emitters.get(qrId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("confirmed")
                        .data(token));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}