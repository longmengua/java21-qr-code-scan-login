package com.example.demo.response;

import com.example.demo.model.LoginType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private String userId;
    private LoginType loginType;
    private String message;
}