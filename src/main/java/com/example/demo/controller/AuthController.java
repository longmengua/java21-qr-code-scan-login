package com.example.demo.controller;

import com.example.demo.enums.BizErrorCode;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.request.LoginRequest;
import com.example.demo.response.LoginResponse;
import com.example.demo.service.AuthService;
import com.example.demo.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public LoginResponse register(
            @RequestBody LoginRequest req,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        if (StringUtil.isBlank(deviceId)) {
            log.info("Missing deviceId");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        req.validateInput();

        return new LoginResponse(authService.register(req.getUsername(), req.getPassword(), req.getLoginType(), deviceId));
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest req,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        if (StringUtil.isBlank(deviceId)) {
            log.info("Missing deviceId");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        req.validateInput();

        return new LoginResponse(authService.login(req.getUsername(), req.getPassword(), req.getLoginType(), deviceId));
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        if (StringUtil.isBlank(refreshToken) || StringUtil.isBlank(deviceId)) {
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        return new LoginResponse(authService.refresh(refreshToken, deviceId));
    }

    @PostMapping("/logout")
    public void logout(
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        if (StringUtil.isBlank(refreshToken)) {
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        authService.logout(refreshToken);
    }
}