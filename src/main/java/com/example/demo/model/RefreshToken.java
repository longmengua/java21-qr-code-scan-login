package com.example.demo.model;

import lombok.*;

@Data
@Builder
public class RefreshToken {
    private String tokenId;
    private String userId;
    private LoginType loginType;
    private String sessionId;
    private String deviceId;
}

