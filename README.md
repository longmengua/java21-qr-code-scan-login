# java21-qr-code-scan-login

## Goal

- ✅ Java 21 
- ✅ Spring Boot 4.0.2 
- ✅ Spring Security 6.3+ 
- ✅ API only 
- ✅ 最高資安等級（Stateless + Server 控控 Session） 
- ✅ Web / App / Scan 登入區分 
- ✅ 互踢規則完整實作 
- ✅ Cache 可切 Local / Redis

## flow

```declarative
[ Client ]
   │
   │  REST API
   ▼
[ Spring Boot 3 API ]
   │
   ├─ Auth Controller
   ├─ QR Login Controller
   ├─ Token Management
   ├─ Session Coordination (核心)
   │
   ├─ Cache Abstraction
   │      ├─ LocalCache
   │      └─ RedisCache
   │
   └─ Security Layer
         ├─ JWT (Access Token)
         ├─ Refresh Token
         ├─ Token Blacklist
         └─ Device / Login-Type Binding
```

## 資安注意事項

- 不能只是「UUID 一掃就登入」
- 必須防： 
  - 重放攻擊
  - QR code 被拍照後重用 
  - Web 被劫持

## 設計原則

- Stateless Access Token + Server-side Session Index

## API

- Web 請求 QR 
  - URL 
    - POST /auth/qr/init
  - Response 
    - qrId (UUID)
    - qrToken (一次性、短效)
  - Error code
    - 待補充
- App 掃碼
  - URL
    - POST /auth/qr/confirm
  - Request
    - qrId
    - qrToken
  - Response
    - 待補充
  - Error code
    - 待補充
- Web 輪詢 or SSE or WebSocket
  - URL
    - GET /auth/qr/status
  - Request
    - qrId
  - Response
      - 待補充
  - Error code
      - 待補充

## 安全矩陣

| 項目                     | 是否符合 |
| ---------------------- | ---- |
| Refresh Token Rotation | ✅    |
| Token 可即時撤銷            | ✅    |
| QR 有 TTL               | ✅    |
| Scan 防重放               | ✅    |
| Device 白名單             | ✅    |
| 單裝置限制                  | ✅    |
| Session 中心化            | ✅    |
| 被盜即止血                  | ✅    |

## Device Fingerprint

```declarative
deviceId = hash(
    deviceUUID +
    os +
    appVersion +
    publicKey
)
```