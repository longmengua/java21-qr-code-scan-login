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

    public boolean isValidatedQrCode() {
        return this.status == QrLoginState.Status.INIT;
    }
}

