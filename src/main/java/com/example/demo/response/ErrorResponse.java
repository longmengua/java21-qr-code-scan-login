package com.example.demo.response;

public record ErrorResponse(
        int code,
        String message,
        String traceId
) {}