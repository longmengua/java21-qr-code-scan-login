curl -X POST http://localhost:9000/auth/qr/confirm \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwIiwibG9naW5UeXBlIjoiQVBQIiwic2Vzc2lvbklkIjoiYWVkZWE1NjQtMWRkMi00OGMxLThhODQtMGZjNmM0ZTRlNTMxIiwiaWF0IjoxNzczNDg0MTgwLCJleHAiOjE3NzM0ODc3ODB9.ntbJ0fhguTpj8IStB9JC72e5wAjo5Sn2qZumhp9nri8" \
  -H "X-Device-Id: DEVICE-001" \
  -d "qrCodeId=f1f3111d-f9be-4fc3-94a5-05f7d4988eb8"