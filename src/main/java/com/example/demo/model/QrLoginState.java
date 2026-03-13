package com.example.demo.model;

import lombok.*;

@Data
@Builder
public class QrLoginState {
    private String userId;
    private String qrCodeId;
    private String qrCodeImg;
    private Status status;

    public enum Status {
        INIT,
        CONFIRMED,
        EXPIRED
    }

    public boolean isValidatedQrCode(String userId) {
        return this.userId.equals(userId) && this.status == QrLoginState.Status.INIT;
    }
}

