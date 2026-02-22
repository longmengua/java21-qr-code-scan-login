package com.example.demo.controller;

import com.example.demo.model.LoginType;
import com.example.demo.request.LoginRequest;
import com.example.demo.response.LoginResponse;
import com.example.demo.service.LoginService;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.service.SessionCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController
 *
 * 提供認證相關 API：
 * 1. 登入 (Web / App)
 * 2. Refresh Token 旋轉
 * 3. 單端登出
 *
 * 每個 endpoint 附上 curl 範例
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;
    private final SessionCoordinator sessionCoordinator;

    /**
     * Web / App 登入
     *
     * RequestBody 範例:
     * {
     *   "username": "test",
     *   "password": "1234",
     *   "loginType": "WEB",
     *   "deviceId": "DEVICE-001"
     * }
     *
     * curl 範例：
     * curl -X POST http://localhost:8080/auth/login \
     *      -H "Content-Type: application/json" \
     *      -d '{"username":"test","password":"1234","loginType":"WEB","deviceId":"DEVICE-001"}'
     */
    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest req
    ) {
        return loginService.login(
                req.username(),
                req.password(),
                req.loginType(),
                req.deviceId()
        );
    }

    /**
     * Refresh Token rotation
     *
     * Headers:
     * - X-Refresh-Token: 後端 refresh token id
     * - X-Device-Id: 裝置識別碼
     *
     * curl 範例：
     * curl -X POST http://localhost:8080/auth/refresh \
     *      -H "X-Refresh-Token: <your-refresh-token-id>" \
     *      -H "X-Device-Id: DEVICE-001"
     */
    @PostMapping("/refresh")
    public LoginResponse refresh(
            @RequestHeader("X-Refresh-Token") String refreshTokenId,
            @RequestHeader("X-Device-Id") String deviceId
    ) {
        return refreshTokenService.refresh(refreshTokenId, deviceId);
    }

    /**
     * 單端登出 (invalidate session)
     *
     * @RequestAttribute 會從過濾器或攔截器中取得 userId 與 loginType
     *
     * curl 範例：
     * curl -X POST http://localhost:8080/auth/logout \
     *      -H "X-User-Id: USER-001" \
     *      -H "X-Login-Type: WEB"
     *
     * ⚠️ 注意：實際 @RequestAttribute 需要在過濾器中注入
     */
    @PostMapping("/logout")
    public void logout(
            @RequestAttribute("userId") String userId,
            @RequestAttribute("loginType") LoginType loginType
    ) {
        sessionCoordinator.invalidate(userId, loginType);
    }
}