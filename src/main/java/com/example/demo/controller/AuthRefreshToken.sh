curl -X POST http://localhost:9000/auth/refresh \
  -H "X-Refresh-Token: <refreshToken>" \
  -H "X-Device-Id: DEVICE-001"