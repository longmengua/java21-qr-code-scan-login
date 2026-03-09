curl -X POST http://localhost:9000/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Device-Id: DEVICE-001" \
  -d '{
    "username": "testuser",
    "password": "1234",
    "loginType": "WEB",
    "deviceId": "DEVICE-001"
  }'