package com.example.demo.request;

import com.example.demo.model.LoginType;

public record LoginRequest(
        String username,
        String password,
        LoginType loginType,
        String deviceId
) {}

