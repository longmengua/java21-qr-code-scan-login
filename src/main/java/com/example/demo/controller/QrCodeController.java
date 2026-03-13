package com.example.demo.controller;

import com.example.demo.enums.BizErrorCode;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.model.LoginType;
import com.example.demo.model.TokenPair;
import com.example.demo.response.QrCodeInitResponse;
import com.example.demo.service.AuthService;
import com.example.demo.service.QrLoginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/qr")
public class QrCodeController {

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
    public QrCodeInitResponse init() {
        String qrCodeId = qrLoginService.init();
        return new QrCodeInitResponse(qrCodeId);
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
            @RequestParam String qrCodeId,
            HttpServletRequest request
    ) {

        // 從 request attribute 取得使用者資訊
        String userId = (String) request.getAttribute("userId");
        LoginType loginType = (LoginType) request.getAttribute("loginType");

        if (loginType != LoginType.APP) {
            throw new BusinessException(BizErrorCode.INVALID_TOKEN);
        }

        TokenPair tokenPair = qrLoginService.confirm(qrCodeId, userId);

        // SSE 通知 Web
        SseEmitter emitter = emitters.get(qrCodeId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("confirmed")
                        .data(tokenPair.getAccessToken()));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}