# Community Service (커뮤니티 서비스)

## 1. 서비스 개요

### 1.1 목적
Community Service는 시그나이트 플랫폼의 모든 커뮤니티 활동을 관리합니다. 게시판, 피드 시스템, 댓글, 좋아요 등 사용자 간 상호작용을 담당합니다.

### 1.2 주요 책임
- 게시글 작성/수정/삭제
- 피드 시스템 관리
- 댓글 시스템
- 좋아요/북마크 기능
- 해시태그 관리
- 신고 및 차단 기능

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: MongoDB (메인), PostgreSQL (관계 데이터)
cache: Redis
storage: AWS S3 (이미지, 파일)
search: Elasticsearch
```

## 3. API 설계

### 3.1 게시글 API

#### POST /api/v1/posts
게시글 작성
```json
// Request
{
  "sigId": "sig-uuid",
  "boardType": "GENERAL", // GENERAL, NOTICE, QNA, GALLERY
  "title": "제목",
  "content": "내용",
  "tags": ["태그1", "태그2"],
  "attachments": ["file-id-1", "file-id-2"],
  "isAnonymous": false
}

// Response
{
  "id": "post-uuid",
  "sigId": "sig-uuid",
  "author": {
    "id": "user-uuid",
    "name": "홍길동",
    "profileImage": "url"
  },
  "title": "제목",
  "boardType": "GENERAL",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### GET /api/v1/posts/{postId}
게시글 상세 조회
```json
// Response
{
  "id": "post-uuid",
  "sigId": "sig-uuid",
  "sig": {
    "id": "sig-uuid",
    "name": "AI 연구 모임"
  },
  "author": {
    "id": "user-uuid",
    "name": "홍길동",
    "profileImage": "url"
  },
  "title": "제목",
  "content": "내용",
  "boardType": "GENERAL",
  "tags": ["태그1", "태그2"],
  "attachments": [
    {
      "id": "file-id",
      "name": "파일명.pdf",
      "size": 1024,
      "url": "download-url"
    }
  ],
  "stats": {
    "views": 123,
    "likes": 45,
    "comments": 12,
    "bookmarks": 5
  },
  "isLiked": true,
  "isBookmarked": false,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### PUT /api/v1/posts/{postId}
게시글 수정
```json
// Request
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "tags": ["새태그1", "새태그2"]
}

// Response
{
  "message": "게시글이 수정되었습니다"
}
```

#### DELETE /api/v1/posts/{postId}
게시글 삭제

### 3.2 피드 API

#### GET /api/v1/feed
피드 조회
```json
// Query Parameters
?type=ALL&sigId=sig-uuid&page=0&size=20

// Response
{
  "content": [
    {
      "type": "POST", // POST, ACTIVITY, VOTE
      "id": "item-uuid",
      "post": {
        "id": "post-uuid",
        "title": "제목",
        "preview": "내용 미리보기...",
        "author": {
          "name": "홍길동"
        },
        "sig": {
          "name": "AI 연구 모임"
        },
        "stats": {
          "likes": 45,
          "comments": 12
        }
      },
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

#### GET /api/v1/feed/trending
인기 피드
```json
// Query Parameters
?period=WEEK&limit=10

// Response
{
  "trending": [
    {
      "rank": 1,
      "post": {
        "id": "post-uuid",
        "title": "인기 게시글",
        "sig": {
          "name": "AI 연구 모임"
        },
        "trendScore": 95.5
      }
    }
  ]
}
```

### 3.3 댓글 API

#### POST /api/v1/posts/{postId}/comments
댓글 작성
```json
// Request
{
  "content": "댓글 내용",
  "parentId": null, // 대댓글인 경우 부모 댓글 ID
  "isAnonymous": false
}

// Response
{
  "id": "comment-uuid",
  "postId": "post-uuid",
  "author": {
    "id": "user-uuid",
    "name": "김철수",
    "profileImage": "url"
  },
  "content": "댓글 내용",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### GET /api/v1/posts/{postId}/comments
댓글 목록 조회
```json
// Query Parameters
?page=0&size=20&sort=NEWEST

// Response
{
  "content": [
    {
      "id": "comment-uuid",
      "author": {
        "id": "user-uuid",
        "name": "김철수",
        "profileImage": "url"
      },
      "content": "댓글 내용",
      "likes": 5,
      "isLiked": false,
      "replies": [
        {
          "id": "reply-uuid",
          "author": {
            "name": "이영희"
          },
          "content": "대댓글 내용",
          "createdAt": "2024-01-01T00:00:00Z"
        }
      ],
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "totalElements": 30,
  "totalPages": 2
}
```

### 3.4 반응 API

#### POST /api/v1/posts/{postId}/like
게시글 좋아요
```json
// Response
{
  "liked": true,
  "likeCount": 46
}
```

#### POST /api/v1/posts/{postId}/bookmark
게시글 북마크
```json
// Response
{
  "bookmarked": true,
  "message": "북마크에 추가되었습니다"
}
```

#### GET /api/v1/users/{userId}/bookmarks
북마크 목록 조회
```json
// Response
{
  "bookmarks": [
    {
      "id": "bookmark-uuid",
      "post": {
        "id": "post-uuid",
        "title": "북마크한 게시글",
        "sig": {
          "name": "AI 연구 모임"
        }
      },
      "bookmarkedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

### 3.5 해시태그 API

#### GET /api/v1/tags/trending
인기 해시태그
```json
// Query Parameters
?period=WEEK&limit=20

// Response
{
  "tags": [
    {
      "name": "AI",
      "count": 234,
      "trend": "UP" // UP, DOWN, STABLE
    },
    {
      "name": "머신러닝",
      "count": 189,
      "trend": "STABLE"
    }
  ]
}
```

#### GET /api/v1/posts/by-tag/{tagName}
해시태그로 게시글 검색
```json
// Query Parameters
?page=0&size=20

// Response
{
  "tag": "AI",
  "posts": {
    "content": [
      {
        "id": "post-uuid",
        "title": "AI 관련 게시글",
        "sig": {
          "name": "AI 연구 모임"
        }
      }
    ],
    "totalElements": 50
  }
}
```

## 4. 데이터베이스 설계

### 4.1 MongoDB 컬렉션

#### posts
```javascript
{
  "_id": ObjectId("..."),
  "sigId": "sig-uuid",
  "authorId": "user-uuid",
  "boardType": "GENERAL",
  "title": "제목",
  "content": "내용",
  "contentHtml": "<p>내용</p>", // 렌더링된 HTML
  "tags": ["태그1", "태그2"],
  "attachments": [
    {
      "id": "file-id",
      "name": "파일명.pdf",
      "size": 1024,
      "mimeType": "application/pdf",
      "url": "s3-url"
    }
  ],
  "stats": {
    "views": 123,
    "likes": 45,
    "comments": 12,
    "bookmarks": 5
  },
  "isAnonymous": false,
  "isDeleted": false,
  "createdAt": ISODate("2024-01-01T00:00:00Z"),
  "updatedAt": ISODate("2024-01-01T00:00:00Z")
}
```

#### comments
```javascript
{
  "_id": ObjectId("..."),
  "postId": "post-id",
  "authorId": "user-uuid",
  "parentId": null, // 대댓글인 경우
  "content": "댓글 내용",
  "likes": 5,
  "isAnonymous": false,
  "isDeleted": false,
  "createdAt": ISODate("2024-01-01T00:00:00Z"),
  "updatedAt": ISODate("2024-01-01T00:00:00Z")
}
```

### 4.2 PostgreSQL 테이블

#### post_likes
```sql
CREATE TABLE post_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id VARCHAR(24) NOT NULL, -- MongoDB ObjectId
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, user_id)
);

CREATE INDEX idx_post_likes_post_id ON post_likes(post_id);
CREATE INDEX idx_post_likes_user_id ON post_likes(user_id);
```

#### bookmarks
```sql
CREATE TABLE bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    post_id VARCHAR(24) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, post_id)
);

CREATE INDEX idx_bookmarks_user_id ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_created_at ON bookmarks(created_at DESC);
```

#### hashtags
```sql
CREATE TABLE hashtags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hashtags_name ON hashtags(name);
CREATE INDEX idx_hashtags_usage_count ON hashtags(usage_count DESC);
```

#### post_reports
```sql
CREATE TABLE post_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id VARCHAR(24) NOT NULL,
    reporter_id UUID NOT NULL,
    reason VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    processed_at TIMESTAMP,
    processed_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 5. 도메인 모델

### 5.1 게시판 유형
```kotlin
enum class BoardType {
    GENERAL,      // 일반 게시판
    NOTICE,       // 공지사항
    QNA,          // 질문답변
    GALLERY,      // 사진첩
    DISCUSSION    // 토론
}
```

### 5.2 피드 아이템 유형
```kotlin
enum class FeedItemType {
    POST,         // 게시글
    ACTIVITY,     // 활동
    VOTE,         // 투표
    ANNOUNCEMENT  // 공지
}
```

### 5.3 신고 사유
```kotlin
enum class ReportReason {
    SPAM,              // 스팸
    INAPPROPRIATE,     // 부적절한 내용
    HARASSMENT,        // 괴롭힘
    FALSE_INFO,        // 허위정보
    COPYRIGHT,         // 저작권 침해
    OTHER             // 기타
}
```

## 6. 서비스 간 통신

### 6.1 이벤트 발행
```kotlin
// 게시글 작성 이벤트
data class PostCreatedEvent(
    val postId: String,
    val sigId: String,
    val authorId: String,
    val boardType: String,
    val timestamp: Instant
)

// 댓글 작성 이벤트
data class CommentCreatedEvent(
    val commentId: String,
    val postId: String,
    val authorId: String,
    val timestamp: Instant
)

// 좋아요 이벤트
data class PostLikedEvent(
    val postId: String,
    val userId: String,
    val timestamp: Instant
)
```

### 6.2 다른 서비스 호출
```kotlin
// User Service 호출
interface UserServiceClient {
    fun getUserInfo(userId: String): UserInfo
    fun getUsersInfo(userIds: List<String>): List<UserInfo>
}

// SIG Service 호출  
interface SigServiceClient {
    fun getSigInfo(sigId: String): SigInfo
    fun checkMembership(sigId: String, userId: String): Boolean
}

// File Service 호출
interface FileServiceClient {
    fun getFileInfo(fileId: String): FileInfo
    fun deleteFiles(fileIds: List<String>)
}
```

## 7. 캐싱 전략

### 7.1 Redis 캐시 구조
```yaml
# 게시글 조회수 캐시
post:views:{postId}:
  - TTL: 1시간
  - 내용: 조회수 (주기적으로 DB 동기화)

# 인기 게시글 캐시
trending:posts:{period}:
  - TTL: 10분
  - 내용: 인기 게시글 목록

# 인기 해시태그 캐시
trending:tags:{period}:
  - TTL: 30분
  - 내용: 인기 해시태그 목록

# 사용자별 좋아요 상태 캐시
user:likes:{userId}:{postId}:
  - TTL: 24시간
  - 내용: true/false
```

### 7.2 캐시 워밍
```kotlin
// 인기 컨텐츠 사전 캐싱
@Scheduled(cron = "0 */10 * * * *")
fun warmTrendingCache() {
    val trendingPosts = calculateTrendingPosts()
    redisTemplate.opsForValue().set(
        "trending:posts:day",
        trendingPosts,
        Duration.ofMinutes(10)
    )
}
```

## 8. 검색 기능 (Elasticsearch)

### 8.1 인덱스 구조
```json
{
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "sigId": { "type": "keyword" },
      "authorId": { "type": "keyword" },
      "title": { 
        "type": "text", 
        "analyzer": "korean",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "content": { 
        "type": "text", 
        "analyzer": "korean" 
      },
      "tags": { "type": "keyword" },
      "boardType": { "type": "keyword" },
      "stats": {
        "properties": {
          "views": { "type": "integer" },
          "likes": { "type": "integer" },
          "comments": { "type": "integer" }
        }
      },
      "createdAt": { "type": "date" }
    }
  }
}
```

### 8.2 검색 쿼리
```kotlin
// 통합 검색
fun searchPosts(query: SearchQuery): SearchResult {
    val searchRequest = SearchRequest.Builder()
        .index("posts")
        .query { q ->
            q.bool { b ->
                // 키워드 검색
                b.must { m ->
                    m.multiMatch { mm ->
                        mm.query(query.keyword)
                            .fields("title^2", "content", "tags")
                    }
                }
                // 필터
                query.sigId?.let {
                    b.filter { f -> f.term { t -> t.field("sigId").value(it) } }
                }
                query.boardType?.let {
                    b.filter { f -> f.term { t -> t.field("boardType").value(it) } }
                }
            }
        }
        .sort { s -> s.field { f -> f.field("createdAt").order(SortOrder.Desc) } }
        .from(query.page * query.size)
        .size(query.size)
        .build()
        
    return elasticsearchClient.search(searchRequest, Post::class.java)
}
```

## 9. 컨텐츠 모더레이션

### 9.1 자동 필터링
```kotlin
// 금지어 필터링
class ContentModerationService {
    private val bannedWords = loadBannedWords()
    
    fun moderateContent(content: String): ModerationResult {
        val violations = mutableListOf<String>()
        
        // 금지어 체크
        bannedWords.forEach { word ->
            if (content.contains(word, ignoreCase = true)) {
                violations.add("금지어 포함: $word")
            }
        }
        
        // 스팸 패턴 체크
        if (isSpamPattern(content)) {
            violations.add("스팸 패턴 감지")
        }
        
        return ModerationResult(
            isApproved = violations.isEmpty(),
            violations = violations
        )
    }
}
```

### 9.2 신고 처리
```kotlin
// 신고 처리 워크플로우
fun processReport(reportId: String, action: ReportAction) {
    val report = reportRepository.findById(reportId)
    
    when (action) {
        ReportAction.HIDE_POST -> {
            // 게시글 숨김
            postService.hidePost(report.postId)
        }
        ReportAction.WARN_USER -> {
            // 사용자 경고
            notificationService.sendWarning(report.authorId)
        }
        ReportAction.BAN_USER -> {
            // 사용자 차단
            userService.banUser(report.authorId)
        }
        ReportAction.DISMISS -> {
            // 신고 기각
            report.status = ReportStatus.DISMISSED
        }
    }
    
    report.processedAt = Instant.now()
    reportRepository.save(report)
}
```

## 10. 성능 최적화

### 10.1 읽기 최적화
```kotlin
// 게시글 목록 조회 최적화
fun getPostList(sigId: String, page: Int): Page<PostSummary> {
    // 1. 필요한 필드만 프로젝션
    val projection = Projections.fields(
        PostSummary::class.java,
        "id", "title", "authorId", "createdAt", "stats"
    )
    
    // 2. 작성자 정보 배치 조회
    val posts = postRepository.findBySigId(sigId, page, projection)
    val authorIds = posts.map { it.authorId }.distinct()
    val authors = userService.getUsersInfo(authorIds)
        .associateBy { it.id }
    
    // 3. 결과 조합
    return posts.map { post ->
        post.copy(author = authors[post.authorId])
    }
}
```

### 10.2 쓰기 최적화
```kotlin
// 조회수 증가 배치 처리
class ViewCountService {
    private val viewCounts = ConcurrentHashMap<String, AtomicInteger>()
    
    fun incrementView(postId: String) {
        viewCounts.computeIfAbsent(postId) { AtomicInteger(0) }
            .incrementAndGet()
    }
    
    @Scheduled(fixedDelay = 60000) // 1분마다
    fun flushViewCounts() {
        val batch = viewCounts.toMap()
        viewCounts.clear()
        
        // 배치 업데이트
        postRepository.batchUpdateViewCounts(batch)
    }
}
```

## 11. 모니터링

### 11.1 비즈니스 메트릭
- 일일 게시글 수
- 활성 사용자 수
- 평균 댓글 수
- 인기 해시태그
- 신고 처리율

### 11.2 기술 메트릭
- API 응답 시간
- 검색 쿼리 성능
- 캐시 히트율
- MongoDB 쿼리 성능
- 파일 업로드 성공률 