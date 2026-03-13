curl -X POST http://localhost:9000/auth/qr/confirm \
  -H "X-Device-Id: DEVICE-001" \
  -d "qrCodeId=abcd-1234-efgh-5678"