# Auth Service (인증 서비스)

## 1. 서비스 개요

### 1.1 목적
Auth Service는 시그나이트 플랫폼의 모든 인증과 인가를 담당하는 핵심 서비스입니다. JWT 토큰 기반의 stateless 인증을 제공하며, OAuth2.0을 통한 소셜 로그인도 지원합니다.

### 1.2 주요 책임
- 사용자 인증 (로그인/로그아웃)
- JWT 토큰 발급 및 검증
- 권한 관리 (RBAC)
- 소셜 로그인 (Google, Kakao, Naver)
- 비밀번호 재설정
- 리프레시 토큰 관리

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: PostgreSQL
cache: Redis
security: Spring Security + JWT
```

## 3. API 설계

### 3.1 인증 API

#### POST /api/v1/auth/login
로그인 요청
```json
// Request
{
  "email": "user@example.com",
  "password": "password123"
}

// Response
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "name": "홍길동",
    "role": "MEMBER"
  }
}
```

#### POST /api/v1/auth/refresh
토큰 갱신
```json
// Request
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}

// Response
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### POST /api/v1/auth/logout
로그아웃
```json
// Request Header
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

// Response
{
  "message": "로그아웃 되었습니다"
}
```

### 3.2 OAuth2.0 소셜 로그인

#### GET /api/v1/auth/oauth/{provider}
소셜 로그인 URL 리다이렉트
- provider: google, kakao, naver

#### GET /api/v1/auth/oauth/{provider}/callback
소셜 로그인 콜백 처리

### 3.3 비밀번호 관리

#### POST /api/v1/auth/password/reset-request
비밀번호 재설정 요청
```json
// Request
{
  "email": "user@example.com"
}

// Response
{
  "message": "비밀번호 재설정 이메일이 발송되었습니다"
}
```

#### POST /api/v1/auth/password/reset
비밀번호 재설정
```json
// Request
{
  "token": "reset-token",
  "newPassword": "newPassword123"
}

// Response
{
  "message": "비밀번호가 재설정되었습니다"
}
```

## 4. 데이터베이스 설계

### 4.1 주요 테이블

#### auth_users
```sql
CREATE TABLE auth_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    provider VARCHAR(50), -- local, google, kakao, naver
    provider_id VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### auth_roles
```sql
CREATE TABLE auth_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### auth_user_roles
```sql
CREATE TABLE auth_user_roles (
    user_id UUID REFERENCES auth_users(id),
    role_id UUID REFERENCES auth_roles(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);
```

#### auth_refresh_tokens
```sql
CREATE TABLE auth_refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth_users(id),
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### auth_password_reset_tokens
```sql
CREATE TABLE auth_password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth_users(id),
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 5. 보안 설계

### 5.1 JWT 토큰 구조
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user-uuid",
    "email": "user@example.com",
    "name": "홍길동",
    "roles": ["MEMBER", "SIG_LEADER"],
    "iat": 1234567890,
    "exp": 1234571490
  }
}
```

### 5.2 토큰 정책
- Access Token: 1시간 유효
- Refresh Token: 7일 유효
- Refresh Token Rotation: 새로운 access token 발급 시 refresh token도 재발급

### 5.3 비밀번호 정책
- 최소 8자 이상
- 대소문자, 숫자, 특수문자 중 3가지 이상 포함
- BCrypt 해싱 (라운드: 10)

## 6. 권한 관리 (RBAC)

### 6.1 기본 역할
```yaml
roles:
  - GUEST: 비회원
  - MEMBER: 일반 회원
  - MENSA_MEMBER: 멘사 인증 회원
  - SIG_MEMBER: SIG 회원
  - SIG_LEADER: SIG 리더
  - ADMIN: 관리자
  - SUPER_ADMIN: 최고 관리자
```

### 6.2 권한 계층
```
SUPER_ADMIN
    └── ADMIN
        └── SIG_LEADER
            └── SIG_MEMBER
                └── MENSA_MEMBER
                    └── MEMBER
                        └── GUEST
```

## 7. 서비스 간 통신

### 7.1 토큰 검증 API (내부용)
```kotlin
// gRPC 서비스 정의
service AuthService {
    rpc ValidateToken(ValidateTokenRequest) returns (ValidateTokenResponse);
    rpc GetUserPermissions(GetPermissionsRequest) returns (GetPermissionsResponse);
}

message ValidateTokenRequest {
    string token = 1;
}

message ValidateTokenResponse {
    bool valid = 1;
    string userId = 2;
    repeated string roles = 3;
}
```

### 7.2 이벤트 발행
```kotlin
// 사용자 로그인 이벤트
data class UserLoggedInEvent(
    val userId: String,
    val email: String,
    val timestamp: Instant,
    val ipAddress: String
)

// 비밀번호 변경 이벤트
data class PasswordChangedEvent(
    val userId: String,
    val timestamp: Instant
)
```

## 8. 에러 처리

### 8.1 에러 코드
```yaml
AUTH001: 잘못된 인증 정보
AUTH002: 만료된 토큰
AUTH003: 유효하지 않은 토큰
AUTH004: 권한 없음
AUTH005: 계정 비활성화
AUTH006: 너무 많은 로그인 시도
AUTH007: 소셜 로그인 실패
AUTH008: 비밀번호 정책 위반
```

### 8.2 에러 응답 형식
```json
{
  "error": {
    "code": "AUTH001",
    "message": "이메일 또는 비밀번호가 올바르지 않습니다",
    "timestamp": "2024-01-01T00:00:00Z"
  }
}
```

## 9. 모니터링 지표

### 9.1 비즈니스 메트릭
- 로그인 성공/실패율
- 평균 로그인 시간
- 활성 세션 수
- 토큰 갱신 빈도

### 9.2 기술 메트릭
- API 응답 시간
- 데이터베이스 쿼리 성능
- Redis 캐시 히트율
- 에러율

## 10. 개발 가이드

### 10.1 프로젝트 구조
```
auth-service/
├── src/main/kotlin/com/signight/auth/
│   ├── config/
│   │   ├── SecurityConfig.kt
│   │   ├── JwtConfig.kt
│   │   └── OAuth2Config.kt
│   ├── controller/
│   │   ├── AuthController.kt
│   │   └── OAuth2Controller.kt
│   ├── service/
│   │   ├── AuthService.kt
│   │   ├── JwtService.kt
│   │   └── OAuth2Service.kt
│   ├── repository/
│   │   ├── UserRepository.kt
│   │   └── TokenRepository.kt
│   ├── domain/
│   │   ├── User.kt
│   │   ├── Role.kt
│   │   └── Token.kt
│   └── AuthServiceApplication.kt
```

### 10.2 환경 변수
```yaml
# JWT 설정
JWT_SECRET: ${JWT_SECRET}
JWT_ACCESS_TOKEN_EXPIRY: 3600
JWT_REFRESH_TOKEN_EXPIRY: 604800

# OAuth2 설정
OAUTH2_GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
OAUTH2_GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET}
OAUTH2_KAKAO_CLIENT_ID: ${KAKAO_CLIENT_ID}
OAUTH2_KAKAO_CLIENT_SECRET: ${KAKAO_CLIENT_SECRET}

# Redis 설정
REDIS_HOST: ${REDIS_HOST}
REDIS_PORT: 6379
REDIS_PASSWORD: ${REDIS_PASSWORD}
```

## 11. 테스트 전략

### 11.1 단위 테스트
- JwtService 토큰 생성/검증
- PasswordEncoder 해싱/검증
- AuthService 비즈니스 로직

### 11.2 통합 테스트
- 로그인 플로우 전체 테스트
- OAuth2 로그인 테스트
- 토큰 갱신 테스트

### 11.3 성능 테스트
- 동시 로그인 부하 테스트
- JWT 검증 성능 테스트
- Redis 캐시 성능 테스트 