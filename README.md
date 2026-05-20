## DB 실행 (Docker)

```bash
cd infra
docker compose up -d
```

---

## Environment Variables (IntelliJ 실행 기준)

IntelliJ → Run → Edit Configurations → Environment variables 에 아래 값 그대로 추가:

```
DB_URL=jdbc:postgresql://localhost:5433/moyeo
DB_USERNAME=moyeo
DB_PASSWORD=moyeo123
```


## OAuth2 Redirect URL 설정

로컬 테스트:
OAUTH2_REDIRECT_URL=http://localhost:8080/oauth/callback

프론트 연동 (로컬):
OAUTH2_REDIRECT_URL=http://localhost:3000/oauth/callback (또는 http://localhost:5173/oauth/callback)

테스트 서버 (ngrok):
OAUTH2_REDIRECT_URL=https://weepily-tinklier-marguerita.ngrok-free.dev/oauth/callback

실제 배포 서버:
OAUTH2_REDIRECT_URL=https://moyeo-fe.vercel.app/oauth/callback


http://localhost:8080/oauth2/authorization/google


http://localhost:8080/swagger-ui/index.html