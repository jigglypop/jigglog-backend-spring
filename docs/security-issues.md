# 보안 문제점 분석

## 1. 민감 정보 하드코딩 🔴

### 문제점
`src/main/resources/application.yml` 파일에 다음과 같은 민감한 정보가 하드코딩되어 있음:

```yaml
spring:
  datasource:
    username: root
    password: 1127star@
    jwt-secret: C877E4A955FBDB2257CCB64154D1D59F1546ED967473C128E477DC9467
    salt: B5F1FB489F6C6CC5A4391B3D186738E8FE23E1F6982D7DFDA77E91337DA61C99CFC64581B84552B74FDECD2
    secretuser: 비공개
    secretpassword: 1127star
  r2dbc:
    password: 1127star
  redis:
    password: redis1127star
cloud:
  aws:
    credentials:
      accessKey: AKIA57XXXXXXXXXX
      secretKey: 2KVjOmXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

### 위험도
- **심각**: 데이터베이스, AWS, JWT 시크릿 등 모든 민감 정보 노출
- **영향**: 전체 시스템 보안 침해 가능

### 해결방안
```kotlin
// 환경변수 또는 외부 설정 파일 사용
@Value("\${DB_PASSWORD:}")
private lateinit var dbPassword: String

@Value("\${JWT_SECRET:}")
private lateinit var jwtSecret: String
```

## 2. CORS 설정 취약점 🔴

### 문제점
```kotlin
// WebSecurityConfig.kt
cors.allowedOrigins = listOf("http://localhost:3000", "https://jigglog.com")
cors.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
cors.allowedHeaders = listOf("*") // 모든 헤더 허용
```

### 위험도
- **보통**: 불필요한 헤더 노출 가능성

### 해결방안
```kotlin
cors.allowedHeaders = listOf("Content-Type", "Authorization", "X-Requested-With")
cors.exposedHeaders = listOf("Authorization") // 필요한 헤더만 노출
```

## 3. JWT 토큰 관리 취약점 🟡

### 문제점
```kotlin
// SecurityService.kt
private val JWT_EXPIRATION_MS = 604800000 // 7일, 너무 긴 만료시간
```

### 위험도
- **보통**: 토큰 탈취 시 장기간 악용 가능

### 해결방안
```kotlin
private val JWT_EXPIRATION_MS = 3600000 // 1시간
private val REFRESH_TOKEN_EXPIRATION_MS = 1209600000 // 14일

// 리프레시 토큰 구현 필요
fun generateRefreshToken(user: User): Mono<String>
```

## 4. 예외 처리 정보 노출 🟡

### 문제점
```kotlin
// AuthHandler.kt
.onErrorResume(Exception::class.java) {
    badRequest().body(Mono.just(it)) // 전체 예외 객체 노출
}
```

### 위험도
- **보통**: 스택 트레이스 및 시스템 정보 노출

### 해결방안
```kotlin
.onErrorResume(Exception::class.java) { e ->
    logger.error("Authentication error", e)
    badRequest().body(Mono.just(mapOf("error" to "인증에 실패했습니다.")))
}
```

## 5. 패스워드 정책 부재 🟡

### 문제점
- 패스워드 복잡도 검증 없음
- 패스워드 히스토리 관리 없음
- 패스워드 만료 정책 없음

### 해결방안
```kotlin
fun validatePassword(password: String): Boolean {
    val pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$"
    return password.matches(pattern.toRegex())
}
```

## 6. SQL 인젝션 예방 🟢

### 현재 상태
R2DBC 사용으로 기본적인 SQL 인젝션 방어는 되어 있음:

```kotlin
@Query("SELECT * FROM user WHERE username = :username")
fun findByUsername(username: String): Mono<User>
```

### 권장사항
복잡한 동적 쿼리 작성 시 추가 검증 필요

## 즉시 조치 필요 항목

1. **환경변수 분리**: 모든 민감 정보를 환경변수로 이전
2. **JWT 시크릿 교체**: 새로운 강력한 시크릿 생성
3. **AWS 키 교체**: 노출된 AWS 키 즉시 교체
4. **예외 처리 개선**: 클라이언트에 노출되는 오류 정보 최소화 