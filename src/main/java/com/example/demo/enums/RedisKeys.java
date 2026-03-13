package com.example.demo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RedisKeys {
    QR_KEY("qr:"),
    SESSION_KEY_PREFIX("user:session:"),
    IMPACT_KEY_PREFIX("impact:"),
    ;

    private final String key;
}
