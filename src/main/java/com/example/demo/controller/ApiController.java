package com.example.demo.controller;

import com.example.demo.model.LoginType;
import com.example.demo.response.ProfileResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProfileController
 *
 * 受保護的使用者資訊 API
 * - 需要 Authorization: Bearer <AccessToken>
 * - AuthFilter 會將 userId 與 loginType 注入 request attribute
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class ApiController {

    @GetMapping("/profile")
    public ProfileResponse profile(HttpServletRequest request) {
        // 從 AuthFilter 注入的 attribute 拿到使用者資訊
        String userId = (String) request.getAttribute("userId");
        LoginType loginType = (LoginType) request.getAttribute("loginType");

        // 回傳簡單示例資訊
        return ProfileResponse.builder()
                .userId(userId)
                .loginType(loginType)
                .message("Hello! This is your profile info.")
                .build();
    }
}