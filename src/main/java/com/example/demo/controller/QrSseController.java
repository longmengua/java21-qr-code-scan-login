package com.example.demo.controller;

import com.example.demo.response.LoginResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth/qr")
public class QrSseController {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping("/subscribe/{qrId}")
    public SseEmitter subscribe(@PathVariable String qrId) {
        SseEmitter emitter = new SseEmitter(120_000L);
        emitters.put(qrId, emitter);
        emitter.onCompletion(() -> emitters.remove(qrId));
        emitter.onTimeout(() -> emitters.remove(qrId));
        return emitter;
    }

    public void notifyConfirmed(String qrId, LoginResponse token) {
        SseEmitter emitter = emitters.get(qrId);
        if (emitter != null) {
            try {
                emitter.send(token);
                emitter.complete();
            } catch (Exception ignored) {}
        }
    }
}

