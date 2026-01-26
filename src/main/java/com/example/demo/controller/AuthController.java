package com.example.demo.controller;

import com.example.demo.model.LoginType;
import com.example.demo.request.LoginRequest;
import com.example.demo.response.LoginResponse;
import com.example.demo.service.LoginService;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.service.SessionCoordinator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;
    private final SessionCoordinator sessionCoordinator;

    public AuthController(
            LoginService loginService,
            RefreshTokenService refreshTokenService,
            SessionCoordinator sessionCoordinator
    ) {
        this.loginService = loginService;
        this.refreshTokenService = refreshTokenService;
        this.sessionCoordinator = sessionCoordinator;
    }

    /**
     * Web / App 登入
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return loginService.login(
                req.username(),
                req.password(),
                req.loginType(),
                req.deviceId()
        );
    }

    /**
     * Refresh Token rotation
     */
    @PostMapping("/refresh")
    public LoginResponse refresh(
            @RequestHeader("X-Refresh-Token") String refreshTokenId,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        return refreshTokenService.refresh(refreshTokenId, deviceId);
    }

    /**
     * 單端登出
     */
    @PostMapping("/logout")
    public void logout(
            @RequestAttribute("userId") String userId,
            @RequestAttribute("loginType") LoginType loginType
    ) {
        sessionCoordinator.invalidate(userId, loginType);
    }
}

