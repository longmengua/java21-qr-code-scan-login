package com.example.demo.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {}

