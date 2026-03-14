package com.example.demo.controller;

import com.example.demo.enums.BizErrorCode;
import com.example.demo.exceptions.BusinessException;
import com.example.demo.model.LoginType;
import com.example.demo.request.LoginRequest;
import com.example.demo.response.LoginResponse;
import com.example.demo.response.SuccessResponse;
import com.example.demo.service.AuthService;
import com.example.demo.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    /**
     * 認證相關服務
     * 負責處理使用者註冊、登入、登出以及 token 生成等邏輯
     */
    private final AuthService authService;

    /**
     * 使用者註冊 API
     *
     * 流程：
     * 1. 驗證請求 header 是否包含 deviceId
     * 2. 驗證 request body 參數
     * 3. 呼叫 AuthService 建立使用者並生成 token
     *
     * @param req      註冊請求資料（username / password / loginType）
     * @param deviceId 裝置 ID，用於識別不同登入設備
     * @return LoginResponse 包含 accessToken
     */
    @PostMapping("/register")
    public LoginResponse register(
            @RequestBody LoginRequest req,
            @RequestHeader("X-Device-Id") String deviceId
    ) {

        // deviceId 是登入裝置識別，用來控制 session 與踢人機制
        if (StringUtil.isBlank(deviceId)) {
            log.info("Missing deviceId");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        // 驗證 username / password / loginType 是否合法
        req.validateInput();

        // 呼叫 service 建立帳號並生成 token
        return new LoginResponse(
                authService.register(
                        req.getUsername(),
                        req.getPassword(),
                        req.getLoginType()
                )
        );
    }

    /**
     * 使用者登入 API
     *
     * 流程：
     * 1. 驗證 deviceId
     * 2. 驗證登入請求參數
     * 3. 呼叫 AuthService 進行帳密驗證
     * 4. 驗證成功後生成 accessToken
     *
     * @param req      登入請求資料
     * @param deviceId 裝置 ID，用於識別登入設備
     * @return LoginResponse 包含 accessToken
     */
    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest req,
            @RequestHeader("X-Device-Id") String deviceId
    ) {

        // 驗證 deviceId 是否存在
        if (StringUtil.isBlank(deviceId)) {
            log.info("Missing deviceId");
            throw new BusinessException(BizErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        // 驗證輸入資料
        req.validateInput();

        // 呼叫 service 驗證帳密並生成 token
        return new LoginResponse(
                authService.login(
                        req.getUsername(),
                        req.getPassword(),
                        req.getLoginType()
                )
        );
    }

    /**
     * 使用者登出 API
     *
     * 說明：
     * - userId 與 loginType 由 JWT Filter 解析後放入 request attribute
     * - logout 會刪除對應的 session cache
     * - 若 session 被刪除，之後的 token 驗證會失敗
     *
     * @param request HTTP request，內含 JWT Filter 注入的 userId 與 loginType
     * @return LogoutResponse 回傳 logout 是否成功
     */
    @PostMapping("/logout")
    public SuccessResponse logout(HttpServletRequest request) {

        // 從 request attribute 取得使用者資訊
        String userId = (String) request.getAttribute("userId");
        LoginType loginType = (LoginType) request.getAttribute("loginType");

        // 若 token 無效或解析失敗
        if (StringUtil.isBlank(userId)) {
            throw new BusinessException(BizErrorCode.AUTH_TOKEN_EXPIRED);
        }

        // 呼叫 service 清除 session
        authService.logout(userId, loginType);

        // 回傳 logout 成功
        return new SuccessResponse(true);
    }
}