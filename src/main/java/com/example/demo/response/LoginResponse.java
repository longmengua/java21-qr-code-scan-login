package com.example.demo.response;

import com.example.demo.model.TokenPair;

public record LoginResponse(
        TokenPair tokenPair
) {}
