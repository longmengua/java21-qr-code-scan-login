package com.example.demo.model;

import lombok.*;

@Data
@Builder
public class QrLoginState {

    public enum Status {
        INIT,
        CONFIRMED,
        EXPIRED
    }

    private String qrId;
    private String qrToken;
    private Status status;

    // 掃碼後綁定的 user
    private String confirmedUserId;
}

