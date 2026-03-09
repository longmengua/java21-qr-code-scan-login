package com.example.demo.model;

import lombok.*;

/**
 * RefreshToken
 *
 * 用於 server-side 管理的 Refresh Token 資料模型。
 *
 * 設計目的：
 * - Refresh Token 本身不存放完整權限資訊
 * - 僅保存識別資訊
 * - 真正有效性由後端 cache / DB 控制
 *
 * 存入：
 * - Redis
 * - 或本地快取（開發環境）
 */
@Data
@Builder
@AllArgsConstructor
public class UserLoginInfo {

    /**
     * Refresh Token 的唯一識別碼
     * - 通常為 UUID
     * - 會作為 cache key 的一部分
     */
    private String tokenId;

    /**
     * 使用者唯一識別碼
     * - 例如 USER-001
     * - 用於識別是哪個 user 發出的 token
     */
    private String userId;

    /**
     * 登入類型
     * - 例如 WEB / APP / ADMIN
     * - 可用於多端登入控制
     */
    private LoginType loginType;

    /**
     * 對應的 sessionId
     * - 用來支援強制登出
     * - 或單端登入限制
     */
    private String sessionId;

    /**
     * 裝置識別碼
     * - 用於區分不同設備
     * - 可實作多設備管理 / 裝置風控
     */
    private String deviceId;
}