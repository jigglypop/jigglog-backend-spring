# SIG Service (SIG 관리 서비스)

## 1. 서비스 개요

### 1.1 목적
SIG Service는 멘사코리아의 Special Interest Groups(SIG) 관리를 담당하는 핵심 서비스입니다. SIG 생성, 회원 관리, 카테고리 관리 등 SIG 관련 모든 기능을 제공합니다.

### 1.2 주요 책임
- SIG 생성 및 관리
- SIG 회원 가입/탈퇴
- SIG 리더 임명/해임
- SIG 카테고리 관리
- SIG 검색 및 추천
- SIG 통계 관리

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: PostgreSQL
cache: Redis
search: Elasticsearch
```

## 3. API 설계

### 3.1 SIG 관리 API

#### POST /api/v1/sigs
SIG 생성
```json
// Request
{
  "name": "AI 연구 모임",
  "description": "인공지능과 머신러닝을 연구하는 모임입니다",
  "category": "ACADEMIC",
  "region": "서울",
  "maxMembers": 50,
  "isPublic": true,
  "tags": ["AI", "머신러닝", "딥러닝"],
  "rules": "모임 규칙...",
  "joinConditions": "멘사 회원만 가입 가능"
}

// Response
{
  "id": "sig-uuid",
  "name": "AI 연구 모임",
  "code": "AI-2024-001",
  "status": "PENDING_APPROVAL",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### GET /api/v1/sigs/{sigId}
SIG 상세 정보 조회
```json
// Response
{
  "id": "sig-uuid",
  "name": "AI 연구 모임",
  "code": "AI-2024-001",
  "description": "인공지능과 머신러닝을 연구하는 모임입니다",
  "category": "ACADEMIC",
  "region": "서울",
  "leader": {
    "id": "user-uuid",
    "name": "홍길동",
    "profileImage": "url"
  },
  "memberCount": 25,
  "maxMembers": 50,
  "status": "ACTIVE",
  "tags": ["AI", "머신러닝", "딥러닝"],
  "statistics": {
    "totalActivities": 45,
    "monthlyActivities": 5,
    "avgAttendance": 18
  },
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### PUT /api/v1/sigs/{sigId}
SIG 정보 수정
```json
// Request
{
  "description": "수정된 설명",
  "maxMembers": 60,
  "tags": ["AI", "머신러닝", "딥러닝", "컴퓨터비전"]
}

// Response
{
  "message": "SIG 정보가 수정되었습니다"
}
```

### 3.2 SIG 회원 관리 API

#### POST /api/v1/sigs/{sigId}/members/join
SIG 가입 신청
```json
// Request
{
  "introduction": "자기소개",
  "motivation": "가입 동기"
}

// Response
{
  "applicationId": "app-uuid",
  "status": "PENDING",
  "message": "가입 신청이 접수되었습니다"
}
```

#### GET /api/v1/sigs/{sigId}/members
SIG 회원 목록 조회
```json
// Query Parameters
?role=MEMBER&status=ACTIVE&page=0&size=20

// Response
{
  "content": [
    {
      "userId": "user-uuid",
      "name": "홍길동",
      "nickname": "길동이",
      "profileImage": "url",
      "role": "MEMBER",
      "joinedAt": "2024-01-01T00:00:00Z",
      "activityScore": 85
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "number": 0,
  "size": 20
}
```

#### PUT /api/v1/sigs/{sigId}/members/{userId}/role
회원 역할 변경
```json
// Request
{
  "role": "MANAGER"
}

// Response
{
  "message": "회원 역할이 변경되었습니다"
}
```

### 3.3 SIG 탐색 API

#### GET /api/v1/sigs/search
SIG 검색
```json
// Query Parameters
?keyword=AI&category=ACADEMIC&region=서울&status=ACTIVE&page=0&size=20

// Response
{
  "content": [
    {
      "id": "sig-uuid",
      "name": "AI 연구 모임",
      "description": "설명...",
      "category": "ACADEMIC",
      "memberCount": 25,
      "tags": ["AI", "머신러닝"],
      "leader": {
        "name": "홍길동"
      }
    }
  ],
  "totalElements": 10,
  "totalPages": 1
}
```

#### GET /api/v1/sigs/recommendations
추천 SIG 목록
```json
// Query Parameters
?userId=user-uuid&limit=5

// Response
{
  "recommendations": [
    {
      "sig": {
        "id": "sig-uuid",
        "name": "AI 연구 모임",
        "category": "ACADEMIC",
        "memberCount": 25
      },
      "reason": "관심사 일치",
      "score": 0.95
    }
  ]
}
```

### 3.4 SIG 가입 신청 관리 API

#### GET /api/v1/sigs/{sigId}/applications
가입 신청 목록 조회 (리더/관리자용)
```json
// Response
{
  "applications": [
    {
      "id": "app-uuid",
      "user": {
        "id": "user-uuid",
        "name": "김철수",
        "memberType": "MENSA_MEMBER"
      },
      "introduction": "자기소개",
      "motivation": "가입 동기",
      "status": "PENDING",
      "appliedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### PUT /api/v1/sigs/{sigId}/applications/{applicationId}
가입 신청 처리
```json
// Request
{
  "action": "APPROVE", // APPROVE, REJECT
  "reason": "승인/거절 사유"
}

// Response
{
  "message": "가입 신청이 처리되었습니다"
}
```

## 4. 데이터베이스 설계

### 4.1 주요 테이블

#### sigs
```sql
CREATE TABLE sigs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL, -- AI-2024-001
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    region VARCHAR(50),
    leader_id UUID NOT NULL,
    max_members INTEGER DEFAULT 50,
    is_public BOOLEAN DEFAULT true,
    status VARCHAR(20) DEFAULT 'PENDING_APPROVAL',
    rules TEXT,
    join_conditions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by UUID
);

CREATE INDEX idx_sigs_category ON sigs(category);
CREATE INDEX idx_sigs_status ON sigs(status);
CREATE INDEX idx_sigs_leader_id ON sigs(leader_id);
```

#### sig_members
```sql
CREATE TABLE sig_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sig_id UUID REFERENCES sigs(id),
    user_id UUID NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER', -- LEADER, MANAGER, MEMBER
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, BANNED
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,
    activity_score INTEGER DEFAULT 0,
    UNIQUE(sig_id, user_id)
);

CREATE INDEX idx_sig_members_sig_id ON sig_members(sig_id);
CREATE INDEX idx_sig_members_user_id ON sig_members(user_id);
CREATE INDEX idx_sig_members_role ON sig_members(role);
```

#### sig_applications
```sql
CREATE TABLE sig_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sig_id UUID REFERENCES sigs(id),
    user_id UUID NOT NULL,
    introduction TEXT,
    motivation TEXT,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    processed_by UUID,
    process_reason TEXT
);

CREATE INDEX idx_sig_applications_sig_id ON sig_applications(sig_id);
CREATE INDEX idx_sig_applications_user_id ON sig_applications(user_id);
CREATE INDEX idx_sig_applications_status ON sig_applications(status);
```

#### sig_categories
```sql
CREATE TABLE sig_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES sig_categories(id),
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### sig_tags
```sql
CREATE TABLE sig_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sig_id UUID REFERENCES sigs(id),
    tag VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sig_id, tag)
);

CREATE INDEX idx_sig_tags_sig_id ON sig_tags(sig_id);
CREATE INDEX idx_sig_tags_tag ON sig_tags(tag);
```

#### sig_statistics
```sql
CREATE TABLE sig_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sig_id UUID REFERENCES sigs(id) UNIQUE,
    total_activities INTEGER DEFAULT 0,
    total_posts INTEGER DEFAULT 0,
    total_votes INTEGER DEFAULT 0,
    avg_attendance DECIMAL(5,2) DEFAULT 0,
    last_activity_at TIMESTAMP,
    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 5. 도메인 모델

### 5.1 SIG 상태
```kotlin
enum class SigStatus {
    PENDING_APPROVAL,  // 승인 대기
    ACTIVE,           // 활성
    INACTIVE,         // 비활성
    SUSPENDED,        // 정지
    CLOSED            // 폐쇄
}
```

### 5.2 회원 역할
```kotlin
enum class SigMemberRole {
    LEADER,    // 리더 (1명)
    MANAGER,   // 관리자 (여러명)
    MEMBER     // 일반 회원
}
```

### 5.3 SIG 카테고리
```kotlin
enum class SigCategory {
    ACADEMIC,     // 학술
    HOBBY,        // 취미
    REGIONAL,     // 지역
    PROFESSIONAL, // 직업
    OTHER        // 기타
}
```

## 6. 서비스 간 통신

### 6.1 이벤트 발행
```kotlin
// SIG 생성 이벤트
data class SigCreatedEvent(
    val sigId: String,
    val name: String,
    val leaderId: String,
    val category: String,
    val timestamp: Instant
)

// 회원 가입 이벤트
data class SigMemberJoinedEvent(
    val sigId: String,
    val userId: String,
    val role: String,
    val timestamp: Instant
)

// SIG 상태 변경 이벤트
data class SigStatusChangedEvent(
    val sigId: String,
    val oldStatus: String,
    val newStatus: String,
    val timestamp: Instant
)
```

### 6.2 다른 서비스 호출
```kotlin
// User Service 호출
interface UserServiceClient {
    fun getUserInfo(userId: String): UserInfo
    fun getUserMemberType(userId: String): MemberType
}

// Activity Service 호출
interface ActivityServiceClient {
    fun getSigActivityCount(sigId: String): Int
    fun getLastActivityDate(sigId: String): LocalDateTime?
}
```

## 7. 캐싱 전략

### 7.1 Redis 캐시 구조
```yaml
# SIG 기본 정보 캐시
sig:info:{sigId}:
  - TTL: 1시간
  - 내용: SIG 기본 정보

# SIG 회원 수 캐시
sig:member:count:{sigId}:
  - TTL: 5분
  - 내용: 회원 수

# 인기 SIG 목록 캐시
sig:popular:
  - TTL: 30분
  - 내용: 인기 SIG Top 10

# 사용자별 가입 SIG 목록 캐시
user:sigs:{userId}:
  - TTL: 10분
  - 내용: 사용자가 가입한 SIG 목록
```

## 8. 검색 기능 (Elasticsearch)

### 8.1 인덱스 구조
```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "name": { "type": "text", "analyzer": "korean" },
      "description": { "type": "text", "analyzer": "korean" },
      "category": { "type": "keyword" },
      "region": { "type": "keyword" },
      "tags": { "type": "keyword" },
      "memberCount": { "type": "integer" },
      "status": { "type": "keyword" },
      "createdAt": { "type": "date" }
    }
  }
}
```

### 8.2 검색 쿼리
```kotlin
// 키워드 검색 + 필터
fun searchSigs(keyword: String?, filters: SigSearchFilter): SearchResult {
    val query = boolQuery()
    
    if (!keyword.isNullOrEmpty()) {
        query.must(multiMatchQuery(keyword)
            .field("name", 2.0f)  // 가중치
            .field("description")
            .field("tags"))
    }
    
    filters.category?.let {
        query.filter(termQuery("category", it))
    }
    
    filters.region?.let {
        query.filter(termQuery("region", it))
    }
    
    return elasticsearchClient.search(query)
}
```

## 9. 추천 시스템

### 9.1 추천 알고리즘
```kotlin
// 협업 필터링 + 콘텐츠 기반 추천
fun recommendSigs(userId: String): List<SigRecommendation> {
    // 1. 사용자의 관심사 분석
    val userInterests = analyzeUserInterests(userId)
    
    // 2. 비슷한 사용자가 가입한 SIG 찾기
    val similarUsers = findSimilarUsers(userId)
    val collaborativeScore = calculateCollaborativeScore(similarUsers)
    
    // 3. 콘텐츠 유사도 계산
    val contentScore = calculateContentSimilarity(userInterests)
    
    // 4. 최종 점수 계산 및 정렬
    return combineScores(collaborativeScore, contentScore)
        .sortedByDescending { it.score }
        .take(10)
}
```

## 10. 배치 작업

### 10.1 SIG 통계 집계
```kotlin
@Scheduled(cron = "0 0 3 * * *")
fun calculateSigStatistics() {
    sigService.getAllActiveSigs().forEach { sig ->
        val stats = SigStatistics(
            totalActivities = activityService.getCount(sig.id),
            totalPosts = communityService.getPostCount(sig.id),
            avgAttendance = activityService.getAvgAttendance(sig.id)
        )
        sigStatisticsRepository.save(stats)
    }
}
```

### 10.2 비활성 SIG 처리
```kotlin
@Scheduled(cron = "0 0 0 1 * *") // 매월 1일
fun processInactiveSigs() {
    val threeMonthsAgo = LocalDateTime.now().minusMonths(3)
    
    sigRepository.findByLastActivityBefore(threeMonthsAgo)
        .forEach { sig ->
            sig.status = SigStatus.INACTIVE
            sigRepository.save(sig)
            
            // 리더에게 알림
            notificationService.sendInactivityNotice(sig)
        }
}
```

## 11. 권한 관리

### 11.1 권한 매트릭스
```yaml
SIG 생성:
  - MENSA_MEMBER 이상

SIG 가입:
  - 공개 SIG: MEMBER 이상
  - 비공개 SIG: 조건에 따름

SIG 관리:
  - 정보 수정: LEADER, MANAGER
  - 회원 승인: LEADER, MANAGER
  - 역할 변경: LEADER
  - SIG 폐쇄: LEADER

관리자 기능:
  - SIG 승인: ADMIN
  - SIG 정지: ADMIN
  - 강제 폐쇄: SUPER_ADMIN
```

## 12. 모니터링

### 12.1 비즈니스 메트릭
- 활성 SIG 수
- 월간 신규 SIG 생성 수
- 평균 SIG 회원 수
- SIG별 활동 지수
- 가입 신청 승인률

### 12.2 기술 메트릭
- API 응답 시간
- 검색 쿼리 성능
- 캐시 히트율
- 데이터베이스 쿼리 성능 