# User Service (사용자 서비스)

## 1. 서비스 개요

### 1.1 목적
User Service는 시그나이트 플랫폼의 사용자 정보와 프로필을 관리하는 서비스입니다. 멘사 회원 인증, 프로필 관리, 회원 등급 관리 등을 담당합니다.

### 1.2 주요 책임
- 사용자 프로필 관리
- 멘사 회원 인증
- 회원 등급 관리
- 사용자 검색
- 프로필 이미지 관리
- 개인정보 보호

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: PostgreSQL
cache: Redis
storage: AWS S3 (프로필 이미지)
```

## 3. API 설계

### 3.1 사용자 프로필 API

#### GET /api/v1/users/{userId}
사용자 정보 조회
```json
// Response
{
  "id": "uuid",
  "email": "user@example.com",
  "name": "홍길동",
  "nickname": "길동이",
  "profileImage": "https://cdn.signight.com/profiles/uuid.jpg",
  "bio": "안녕하세요. 홍길동입니다.",
  "memberType": "MENSA_MEMBER",
  "mensaId": "M12345",
  "joinedAt": "2024-01-01T00:00:00Z",
  "stats": {
    "sigCount": 3,
    "postCount": 45,
    "commentCount": 123
  }
}
```

#### PUT /api/v1/users/{userId}
사용자 정보 수정
```json
// Request
{
  "nickname": "새로운닉네임",
  "bio": "자기소개 수정",
  "phoneNumber": "010-1234-5678"
}

// Response
{
  "message": "프로필이 수정되었습니다"
}
```

#### POST /api/v1/users/{userId}/profile-image
프로필 이미지 업로드
```
// Request
Content-Type: multipart/form-data
file: image.jpg

// Response
{
  "imageUrl": "https://cdn.signight.com/profiles/uuid.jpg"
}
```

### 3.2 멘사 회원 인증 API

#### POST /api/v1/users/{userId}/mensa-verification
멘사 회원 인증 요청
```json
// Request
{
  "mensaId": "M12345",
  "verificationDocument": "file-id"
}

// Response
{
  "status": "PENDING",
  "message": "멘사 회원 인증 요청이 접수되었습니다"
}
```

#### GET /api/v1/users/{userId}/mensa-verification
멘사 회원 인증 상태 조회
```json
// Response
{
  "status": "VERIFIED", // PENDING, VERIFIED, REJECTED
  "mensaId": "M12345",
  "verifiedAt": "2024-01-01T00:00:00Z",
  "expiresAt": "2025-01-01T00:00:00Z"
}
```

### 3.3 사용자 검색 API

#### GET /api/v1/users/search
사용자 검색
```json
// Query Parameters
?keyword=홍길동&memberType=MENSA_MEMBER&page=0&size=20

// Response
{
  "content": [
    {
      "id": "uuid",
      "name": "홍길동",
      "nickname": "길동이",
      "profileImage": "url",
      "memberType": "MENSA_MEMBER"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

### 3.4 회원 통계 API

#### GET /api/v1/users/{userId}/statistics
사용자 활동 통계
```json
// Response
{
  "userId": "uuid",
  "statistics": {
    "totalPosts": 45,
    "totalComments": 123,
    "totalLikes": 567,
    "joinedSigs": 3,
    "leadingSigs": 1,
    "activityScore": 890,
    "monthlyActivity": [
      {
        "month": "2024-01",
        "posts": 5,
        "comments": 15,
        "participations": 3
      }
    ]
  }
}
```

## 4. 데이터베이스 설계

### 4.1 주요 테이블

#### users
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_user_id UUID UNIQUE NOT NULL, -- Auth Service 연동
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) UNIQUE,
    phone_number VARCHAR(20),
    profile_image_url VARCHAR(500),
    bio TEXT,
    member_type VARCHAR(50) DEFAULT 'MEMBER',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_nickname ON users(nickname);
CREATE INDEX idx_users_member_type ON users(member_type);
```

#### mensa_verifications
```sql
CREATE TABLE mensa_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    mensa_id VARCHAR(50) UNIQUE,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED
    verification_document_url VARCHAR(500),
    verified_at TIMESTAMP,
    verified_by UUID,
    expires_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mensa_verifications_user_id ON mensa_verifications(user_id);
CREATE INDEX idx_mensa_verifications_status ON mensa_verifications(status);
```

#### user_statistics
```sql
CREATE TABLE user_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) UNIQUE,
    total_posts INTEGER DEFAULT 0,
    total_comments INTEGER DEFAULT 0,
    total_likes INTEGER DEFAULT 0,
    total_sigs_joined INTEGER DEFAULT 0,
    total_sigs_leading INTEGER DEFAULT 0,
    activity_score INTEGER DEFAULT 0,
    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_statistics_user_id ON user_statistics(user_id);
```

#### user_privacy_settings
```sql
CREATE TABLE user_privacy_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) UNIQUE,
    show_email BOOLEAN DEFAULT false,
    show_phone BOOLEAN DEFAULT false,
    show_activity BOOLEAN DEFAULT true,
    show_sigs BOOLEAN DEFAULT true,
    searchable BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 5. 도메인 모델

### 5.1 회원 유형
```kotlin
enum class MemberType {
    GUEST,           // 비회원
    MEMBER,          // 일반 회원
    MENSA_MEMBER,    // 멘사 인증 회원
    INACTIVE         // 비활성 회원
}
```

### 5.2 멘사 인증 상태
```kotlin
enum class MensaVerificationStatus {
    PENDING,    // 대기중
    VERIFIED,   // 인증완료
    REJECTED,   // 거부됨
    EXPIRED     // 만료됨
}
```

### 5.3 사용자 엔티티
```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(unique = true, nullable = false)
    val authUserId: UUID,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(nullable = false)
    var name: String,
    
    @Column(unique = true)
    var nickname: String? = null,
    
    var phoneNumber: String? = null,
    var profileImageUrl: String? = null,
    var bio: String? = null,
    
    @Enumerated(EnumType.STRING)
    var memberType: MemberType = MemberType.MEMBER,
    
    var isActive: Boolean = true,
    
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
```

## 6. 서비스 간 통신

### 6.1 이벤트 발행
```kotlin
// 사용자 가입 완료 이벤트
data class UserRegisteredEvent(
    val userId: String,
    val email: String,
    val name: String,
    val timestamp: Instant
)

// 멘사 인증 완료 이벤트
data class MensaVerifiedEvent(
    val userId: String,
    val mensaId: String,
    val verifiedAt: Instant
)

// 프로필 업데이트 이벤트
data class UserProfileUpdatedEvent(
    val userId: String,
    val updatedFields: List<String>,
    val timestamp: Instant
)
```

### 6.2 이벤트 구독
```kotlin
// Auth Service로부터 사용자 생성 이벤트 수신
@EventListener
fun handleUserCreatedEvent(event: UserCreatedEvent) {
    // 새 사용자 프로필 생성
    userService.createUserProfile(event.authUserId, event.email)
}

// Community Service로부터 활동 이벤트 수신
@EventListener
fun handleUserActivityEvent(event: UserActivityEvent) {
    // 사용자 통계 업데이트
    statisticsService.updateUserStatistics(event.userId, event.activityType)
}
```

## 7. 캐싱 전략

### 7.1 Redis 캐시 구조
```yaml
# 사용자 프로필 캐시
user:profile:{userId}:
  - TTL: 1시간
  - 내용: 사용자 기본 정보

# 사용자 통계 캐시
user:stats:{userId}:
  - TTL: 5분
  - 내용: 활동 통계

# 멘사 인증 상태 캐시
user:mensa:{userId}:
  - TTL: 24시간
  - 내용: 멘사 인증 정보
```

### 7.2 캐시 무효화
- 프로필 수정 시 즉시 무효화
- 통계는 5분 주기로 갱신
- 멘사 인증 변경 시 즉시 무효화

## 8. 보안 및 개인정보

### 8.1 개인정보 보호
- 민감 정보 암호화 (전화번호, 주민번호)
- 개인정보 조회 권한 체크
- 개인정보 다운로드 기능
- 회원 탈퇴 시 데이터 처리

### 8.2 접근 권한
```yaml
권한 매트릭스:
  - 본인 프로필: 전체 조회/수정 가능
  - 타인 프로필: 공개 정보만 조회
  - 관리자: 전체 조회 가능
  - 멘사 인증: 관리자만 승인 가능
```

## 9. 모니터링

### 9.1 비즈니스 메트릭
- 일일 신규 가입자 수
- 멘사 인증 요청/승인률
- 프로필 완성도
- 활성 사용자 비율

### 9.2 기술 메트릭
- API 응답 시간
- 캐시 히트율
- 데이터베이스 쿼리 성능
- 이미지 업로드 성공률

## 10. 배치 작업

### 10.1 사용자 통계 집계
```kotlin
// 매일 새벽 2시 실행
@Scheduled(cron = "0 0 2 * * *")
fun calculateUserStatistics() {
    // 모든 사용자의 활동 통계 재계산
    userService.recalculateAllUserStatistics()
}
```

### 10.2 비활성 계정 처리
```kotlin
// 매주 일요일 실행
@Scheduled(cron = "0 0 0 * * SUN")
fun processInactiveAccounts() {
    // 6개월 이상 미접속 계정 비활성화
    userService.deactivateInactiveAccounts(months = 6)
}
```

### 10.3 멘사 인증 만료 처리
```kotlin
// 매일 실행
@Scheduled(cron = "0 0 0 * * *")
fun processMensaVerificationExpiry() {
    // 만료된 멘사 인증 처리
    mensaService.processExpiredVerifications()
}
```

## 11. 개발 가이드

### 11.1 API 응답 표준
```kotlin
// 성공 응답
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val message: String? = null
)

// 에러 응답
data class ErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail
)

data class ErrorDetail(
    val code: String,
    val message: String,
    val field: String? = null
)
```

### 11.2 유효성 검증
```kotlin
// 닉네임 규칙
- 2-20자
- 한글, 영문, 숫자만 허용
- 특수문자 불가

// 자기소개 규칙
- 최대 500자
- 금지어 필터링

// 전화번호 규칙
- 한국 휴대폰 번호 형식
- 중복 불가
``` 