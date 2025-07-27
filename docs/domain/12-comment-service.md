# Comment Service (댓글 서비스)

## 1. 서비스 개요

### 1.1 목적
Comment Service는 시그나이트 플랫폼의 모든 댓글 시스템을 관리합니다. 게시글 댓글, 대댓글, 댓글 반응, 댓글 신고 등 사용자 상호작용을 지원합니다.

### 1.2 주요 책임
- 댓글 및 대댓글 CRUD
- 댓글 계층 구조 관리
- 댓글 반응 (좋아요, 신고)
- 댓글 알림 시스템
- 댓글 모더레이션
- 댓글 검색 및 필터링

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: MongoDB (메인), PostgreSQL (신고/모더레이션)
cache: Redis
realtime: WebSocket (STOMP)
search: Elasticsearch
```

## 3. 댓글 시스템 설계

### 3.1 댓글 계층 구조
```
게시글 (Post)
├── 댓글 1 (Comment)
│   ├── 대댓글 1-1 (Reply)
│   ├── 대댓글 1-2 (Reply)
│   │   └── 대댓글 1-2-1 (Reply to Reply)
│   └── 대댓글 1-3 (Reply)
├── 댓글 2 (Comment)
│   └── 대댓글 2-1 (Reply)
└── 댓글 3 (Comment)
```

### 3.2 댓글 유형
- **일반 댓글**: 게시글에 직접 작성되는 댓글
- **대댓글**: 댓글에 대한 답글
- **익명 댓글**: 익명으로 작성되는 댓글
- **핀 댓글**: 상단에 고정되는 중요 댓글

## 4. API 설계

### 4.1 댓글 관리 API

#### POST /api/v1/comments
댓글 작성
```json
// Request
{
  "postId": "post-uuid",
  "parentCommentId": null, // 대댓글인 경우 부모 댓글 ID
  "content": "정말 유익한 글이네요! 감사합니다.",
  "isAnonymous": false,
  "attachments": [
    {
      "fileId": "file-uuid",
      "type": "IMAGE",
      "description": "참고 이미지"
    }
  ],
  "mentions": [
    {
      "userId": "user-uuid",
      "username": "김철수",
      "position": {
        "start": 15,
        "end": 18
      }
    }
  ]
}

// Response
{
  "id": "comment-uuid",
  "postId": "post-uuid",
  "parentCommentId": null,
  "content": "정말 유익한 글이네요! 감사합니다.",
  "author": {
    "id": "user-uuid",
    "name": "홍길동",
    "profileImage": "url",
    "memberType": "MENSA_MEMBER"
  },
  "isAnonymous": false,
  "level": 0,
  "path": "comment-uuid",
  "createdAt": "2024-11-15T10:30:00Z",
  "status": "PUBLISHED"
}
```

#### GET /api/v1/posts/{postId}/comments
게시글 댓글 목록 조회
```json
// Query Parameters
?sort=LATEST&page=0&size=20&includeReplies=true&maxDepth=3

// Response
{
  "comments": [
    {
      "id": "comment-uuid-1",
      "postId": "post-uuid",
      "content": "정말 유익한 글이네요!",
      "author": {
        "id": "user-uuid",
        "name": "홍길동",
        "profileImage": "url"
      },
      "isAnonymous": false,
      "level": 0,
      "reactions": {
        "likes": 5,
        "dislikes": 0
      },
      "userReaction": "LIKE",
      "isPinned": false,
      "replyCount": 2,
      "createdAt": "2024-11-15T10:30:00Z",
      "updatedAt": null,
      "replies": [
        {
          "id": "comment-uuid-2",
          "parentCommentId": "comment-uuid-1",
          "content": "@홍길동 맞습니다! 저도 도움이 많이 됐어요.",
          "author": {
            "name": "김철수"
          },
          "level": 1,
          "mentions": [
            {
              "userId": "user-uuid",
              "username": "홍길동"
            }
          ],
          "reactions": {
            "likes": 2,
            "dislikes": 0
          },
          "createdAt": "2024-11-15T11:00:00Z"
        }
      ]
    }
  ],
  "totalElements": 45,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "commentStatistics": {
    "totalComments": 45,
    "totalReplies": 23,
    "averageRating": 4.2
  }
}
```

#### GET /api/v1/comments/{commentId}
댓글 상세 조회
```json
// Response
{
  "id": "comment-uuid",
  "postId": "post-uuid",
  "parentCommentId": null,
  "content": "정말 유익한 글이네요! 감사합니다.",
  "contentHtml": "<p>정말 유익한 글이네요! 감사합니다.</p>",
  "author": {
    "id": "user-uuid",
    "name": "홍길동",
    "nickname": "길동이",
    "profileImage": "url",
    "memberType": "MENSA_MEMBER",
    "badgeLevel": "GOLD"
  },
  "isAnonymous": false,
  "level": 0,
  "path": "comment-uuid",
  "attachments": [
    {
      "id": "attachment-uuid",
      "type": "IMAGE",
      "fileName": "reference.jpg",
      "fileSize": 1024000,
      "thumbnailUrl": "url",
      "downloadUrl": "url"
    }
  ],
  "mentions": [
    {
      "userId": "user-uuid-2",
      "username": "김철수",
      "position": {
        "start": 15,
        "end": 18
      }
    }
  ],
  "reactions": {
    "likes": 12,
    "dislikes": 1,
    "helpful": 5,
    "insightful": 3
  },
  "userReaction": "LIKE",
  "isPinned": false,
  "isEdited": false,
  "replyCount": 5,
  "status": "PUBLISHED",
  "moderationInfo": {
    "isReported": false,
    "reportCount": 0,
    "moderationStatus": "APPROVED"
  },
  "createdAt": "2024-11-15T10:30:00Z",
  "updatedAt": null
}
```

#### PUT /api/v1/comments/{commentId}
댓글 수정
```json
// Request
{
  "content": "정말 유익한 글이네요! 감사합니다. (수정됨)",
  "attachments": [
    {
      "fileId": "file-uuid-new",
      "type": "IMAGE",
      "description": "업데이트된 참고 이미지"
    }
  ]
}

// Response
{
  "success": true,
  "updatedAt": "2024-11-15T12:00:00Z",
  "isEdited": true,
  "editHistory": {
    "editCount": 1,
    "lastEditedAt": "2024-11-15T12:00:00Z"
  }
}
```

#### DELETE /api/v1/comments/{commentId}
댓글 삭제
```json
// Query Parameters
?reason=inappropriate&deleteReplies=false

// Response
{
  "success": true,
  "deletedAt": "2024-11-15T13:00:00Z",
  "affectedReplies": 0, // 함께 삭제된 대댓글 수
  "isRecoverable": true
}
```

### 4.2 댓글 반응 API

#### POST /api/v1/comments/{commentId}/reactions
댓글 반응 추가
```json
// Request
{
  "type": "LIKE" // LIKE, DISLIKE, HELPFUL, INSIGHTFUL
}

// Response
{
  "success": true,
  "reactions": {
    "likes": 13,
    "dislikes": 1,
    "helpful": 5,
    "insightful": 3
  },
  "userReaction": "LIKE",
  "reactionChange": "+1" // 이전 반응과의 차이
}
```

#### GET /api/v1/comments/{commentId}/reactions
댓글 반응 목록
```json
// Response
{
  "reactions": [
    {
      "type": "LIKE",
      "count": 13,
      "users": [
        {
          "userId": "user-uuid",
          "name": "김철수",
          "reactedAt": "2024-11-15T11:00:00Z"
        }
      ]
    },
    {
      "type": "HELPFUL",
      "count": 5,
      "users": [
        {
          "userId": "user-uuid-2",
          "name": "이영희",
          "reactedAt": "2024-11-15T11:30:00Z"
        }
      ]
    }
  ],
  "totalReactions": 22,
  "userReaction": "LIKE"
}
```

### 4.3 댓글 검색 API

#### GET /api/v1/comments/search
댓글 검색
```json
// Query Parameters
?query=감사합니다&postId=post-uuid&authorId=user-uuid&startDate=2024-11-01&endDate=2024-11-30

// Response
{
  "results": [
    {
      "comment": {
        "id": "comment-uuid",
        "content": "정말 유익한 글이네요! 감사합니다.",
        "author": {
          "name": "홍길동"
        },
        "postTitle": "ChatGPT API 활용 가이드",
        "createdAt": "2024-11-15T10:30:00Z"
      },
      "highlight": "정말 유익한 글이네요! <em>감사합니다</em>.",
      "relevanceScore": 0.95
    }
  ],
  "totalResults": 15,
  "searchTime": 25
}
```

### 4.4 댓글 신고 API

#### POST /api/v1/comments/{commentId}/reports
댓글 신고
```json
// Request
{
  "reason": "INAPPROPRIATE_CONTENT", // SPAM, HARASSMENT, INAPPROPRIATE_CONTENT, COPYRIGHT
  "description": "부적절한 언어 사용",
  "screenshots": ["file-uuid-1", "file-uuid-2"]
}

// Response
{
  "reportId": "report-uuid",
  "status": "SUBMITTED",
  "submittedAt": "2024-11-15T14:00:00Z",
  "estimatedReviewTime": "24시간 이내"
}
```

#### GET /api/v1/comments/{commentId}/reports
댓글 신고 현황 (관리자만)
```json
// Response
{
  "commentId": "comment-uuid",
  "reportCount": 3,
  "reports": [
    {
      "id": "report-uuid",
      "reporterId": "user-uuid",
      "reason": "INAPPROPRIATE_CONTENT",
      "description": "부적절한 언어 사용",
      "status": "PENDING",
      "submittedAt": "2024-11-15T14:00:00Z"
    }
  ],
  "moderationStatus": "UNDER_REVIEW",
  "autoModerationFlags": [
    "PROFANITY_DETECTED",
    "SPAM_PATTERNS"
  ]
}
```

### 4.5 실시간 댓글 API (WebSocket)

#### 댓글 실시간 업데이트
```javascript
// WebSocket 연결
ws://api.signight.com/ws/comments

// 새 댓글 알림
{
  "type": "COMMENT_ADDED",
  "data": {
    "postId": "post-uuid",
    "comment": {
      "id": "comment-uuid",
      "content": "새로운 댓글입니다",
      "author": {
        "name": "홍길동"
      },
      "createdAt": "2024-11-15T15:00:00Z"
    }
  }
}

// 댓글 반응 업데이트
{
  "type": "COMMENT_REACTION_UPDATED",
  "data": {
    "commentId": "comment-uuid",
    "reactions": {
      "likes": 14,
      "dislikes": 1
    },
    "recentReaction": {
      "type": "LIKE",
      "userId": "user-uuid",
      "userName": "김철수"
    }
  }
}

// 댓글 삭제 알림
{
  "type": "COMMENT_DELETED",
  "data": {
    "commentId": "comment-uuid",
    "postId": "post-uuid",
    "deletedAt": "2024-11-15T16:00:00Z"
  }
}
```

## 5. 데이터베이스 설계

### 5.1 MongoDB 컬렉션

#### comments
```javascript
{
  "_id": ObjectId("..."),
  "postId": "post-uuid",
  "parentCommentId": null, // 대댓글인 경우 부모 댓글 ID
  "authorId": "user-uuid",
  "content": "정말 유익한 글이네요! 감사합니다.",
  "contentHtml": "<p>정말 유익한 글이네요! 감사합니다.</p>",
  "isAnonymous": false,
  "level": 0, // 댓글 깊이 (0: 댓글, 1: 대댓글, 2: 대대댓글)
  "path": "comment-uuid", // 계층 경로
  "attachments": [
    {
      "id": "attachment-uuid",
      "fileId": "file-uuid",
      "type": "IMAGE",
      "fileName": "reference.jpg",
      "fileSize": 1024000,
      "thumbnailUrl": "url",
      "description": "참고 이미지"
    }
  ],
  "mentions": [
    {
      "userId": "user-uuid",
      "username": "김철수",
      "position": {
        "start": 15,
        "end": 18
      }
    }
  ],
  "reactions": {
    "likes": 12,
    "dislikes": 1,
    "helpful": 5,
    "insightful": 3
  },
  "isPinned": false,
  "isEdited": false,
  "editHistory": [
    {
      "editedAt": ISODate("2024-11-15T12:00:00Z"),
      "previousContent": "이전 내용"
    }
  ],
  "replyCount": 5,
  "status": "PUBLISHED", // PUBLISHED, HIDDEN, DELETED, PENDING_REVIEW
  "moderationInfo": {
    "isReported": false,
    "reportCount": 0,
    "moderationStatus": "APPROVED",
    "autoModerationFlags": [],
    "moderatedAt": null,
    "moderatedBy": null
  },
  "ipAddress": "192.168.1.1", // 보안용
  "userAgent": "Mozilla/5.0...",
  "createdAt": ISODate("2024-11-15T10:30:00Z"),
  "updatedAt": null,
  "deletedAt": null
}
```

### 5.2 PostgreSQL 테이블

#### comment_reactions
```sql
CREATE TABLE comment_reactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comment_id VARCHAR(24) NOT NULL, -- MongoDB ObjectId
    user_id UUID NOT NULL,
    reaction_type VARCHAR(20) NOT NULL, -- LIKE, DISLIKE, HELPFUL, INSIGHTFUL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(comment_id, user_id)
);

CREATE INDEX idx_comment_reactions_comment_id ON comment_reactions(comment_id);
CREATE INDEX idx_comment_reactions_user_id ON comment_reactions(user_id);
CREATE INDEX idx_comment_reactions_type ON comment_reactions(reaction_type);
```

#### comment_reports
```sql
CREATE TABLE comment_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comment_id VARCHAR(24) NOT NULL,
    reporter_id UUID NOT NULL,
    reason VARCHAR(50) NOT NULL, -- SPAM, HARASSMENT, INAPPROPRIATE_CONTENT, COPYRIGHT
    description TEXT,
    screenshots TEXT[], -- 첨부된 스크린샷 파일 ID들
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, REVIEWED, RESOLVED, DISMISSED
    reviewed_by UUID,
    reviewed_at TIMESTAMP,
    resolution_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comment_reports_comment_id ON comment_reports(comment_id);
CREATE INDEX idx_comment_reports_reporter_id ON comment_reports(reporter_id);
CREATE INDEX idx_comment_reports_status ON comment_reports(status);
```

#### comment_moderation_log
```sql
CREATE TABLE comment_moderation_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comment_id VARCHAR(24) NOT NULL,
    moderator_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL, -- HIDE, DELETE, APPROVE, PIN, UNPIN
    reason TEXT,
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_moderation_log_comment_id ON comment_moderation_log(comment_id);
CREATE INDEX idx_moderation_log_moderator_id ON comment_moderation_log(moderator_id);
```

#### comment_analytics
```sql
CREATE TABLE comment_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id VARCHAR(24) NOT NULL,
    analytics_date DATE NOT NULL,
    total_comments INTEGER DEFAULT 0,
    total_replies INTEGER DEFAULT 0,
    total_reactions INTEGER DEFAULT 0,
    average_sentiment DECIMAL(3,2), -- -1.0 ~ 1.0
    top_commenters JSONB,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, analytics_date)
);

CREATE INDEX idx_comment_analytics_post_date ON comment_analytics(post_id, analytics_date);
```

## 6. 도메인 모델

### 6.1 댓글 상태
```kotlin
enum class CommentStatus {
    PUBLISHED,      // 발행됨
    HIDDEN,         // 숨김 (모더레이션)
    DELETED,        // 삭제됨
    PENDING_REVIEW  // 검토 대기중
}
```

### 6.2 반응 유형
```kotlin
enum class CommentReactionType {
    LIKE,        // 좋아요
    DISLIKE,     // 싫어요
    HELPFUL,     // 도움돼요
    INSIGHTFUL   // 통찰력있어요
}
```

### 6.3 신고 사유
```kotlin
enum class ReportReason {
    SPAM,                  // 스팸
    HARASSMENT,            // 괴롭힘
    INAPPROPRIATE_CONTENT, // 부적절한 내용
    COPYRIGHT,            // 저작권 침해
    HATE_SPEECH,          // 혐오 발언
    FALSE_INFORMATION     // 거짓 정보
}
```

### 6.4 댓글 엔티티
```kotlin
@Document(collection = "comments")
data class Comment(
    @Id
    val id: String = ObjectId().toString(),
    
    val postId: String,
    val parentCommentId: String? = null,
    val authorId: String,
    
    var content: String,
    var contentHtml: String,
    val isAnonymous: Boolean = false,
    
    val level: Int = 0,
    val path: String, // 계층 경로 표현
    
    var attachments: List<CommentAttachment> = emptyList(),
    var mentions: List<CommentMention> = emptyList(),
    
    val reactions: CommentReactions = CommentReactions(),
    
    var isPinned: Boolean = false,
    var isEdited: Boolean = false,
    val editHistory: MutableList<CommentEdit> = mutableListOf(),
    
    var replyCount: Int = 0,
    var status: CommentStatus = CommentStatus.PUBLISHED,
    
    val moderationInfo: CommentModerationInfo = CommentModerationInfo(),
    
    val ipAddress: String? = null,
    val userAgent: String? = null,
    
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null,
    
    var deletedAt: LocalDateTime? = null
) {
    fun isRootComment(): Boolean = parentCommentId == null
    fun isReply(): Boolean = parentCommentId != null
    fun canBeEdited(): Boolean = createdAt.isAfter(LocalDateTime.now().minusHours(24))
    
    fun getThreadPath(): List<String> {
        return path.split("/").filter { it.isNotEmpty() }
    }
}

data class CommentAttachment(
    val id: String,
    val fileId: String,
    val type: String, // IMAGE, FILE
    val fileName: String,
    val fileSize: Long,
    val thumbnailUrl: String? = null,
    val description: String? = null
)

data class CommentMention(
    val userId: String,
    val username: String,
    val position: MentionPosition
)

data class MentionPosition(
    val start: Int,
    val end: Int
)

data class CommentReactions(
    var likes: Long = 0,
    var dislikes: Long = 0,
    var helpful: Long = 0,
    var insightful: Long = 0
) {
    fun getTotalReactions(): Long = likes + dislikes + helpful + insightful
    fun getPositiveRatio(): Double = 
        if (getTotalReactions() == 0L) 0.0 
        else (likes + helpful + insightful).toDouble() / getTotalReactions()
}

data class CommentEdit(
    val editedAt: LocalDateTime,
    val previousContent: String,
    val editReason: String? = null
)

data class CommentModerationInfo(
    var isReported: Boolean = false,
    var reportCount: Int = 0,
    var moderationStatus: String = "APPROVED",
    val autoModerationFlags: MutableList<String> = mutableListOf(),
    var moderatedAt: LocalDateTime? = null,
    var moderatedBy: String? = null
)
```

## 7. 계층 구조 관리

### 7.1 댓글 트리 서비스
```kotlin
@Service
class CommentTreeService(
    private val commentRepository: CommentRepository
) {
    
    fun getCommentTree(
        postId: String,
        maxDepth: Int = 3,
        sort: CommentSortType = CommentSortType.LATEST
    ): List<CommentTreeNode> {
        
        // 루트 댓글들 조회
        val rootComments = commentRepository.findRootCommentsByPostId(postId, sort)
        
        return rootComments.map { rootComment ->
            buildCommentTree(rootComment, maxDepth, 1)
        }
    }
    
    private fun buildCommentTree(
        comment: Comment,
        maxDepth: Int,
        currentDepth: Int
    ): CommentTreeNode {
        
        val replies = if (currentDepth < maxDepth) {
            commentRepository.findRepliesByParentId(comment.id)
                .map { reply ->
                    buildCommentTree(reply, maxDepth, currentDepth + 1)
                }
        } else {
            emptyList()
        }
        
        return CommentTreeNode(
            comment = comment,
            replies = replies,
            hasMoreReplies = comment.replyCount > replies.size
        )
    }
    
    fun addReply(parentCommentId: String, reply: Comment): Comment {
        val parentComment = commentRepository.findById(parentCommentId)
        
        // 경로 설정
        reply.path = "${parentComment.path}/${reply.id}"
        reply.level = parentComment.level + 1
        
        // 최대 깊이 제한 (5단계)
        if (reply.level > 5) {
            throw CommentDepthExceededException("댓글은 최대 5단계까지만 가능합니다")
        }
        
        val savedReply = commentRepository.save(reply)
        
        // 부모 댓글의 답글 수 증가
        commentRepository.incrementReplyCount(parentCommentId)
        
        return savedReply
    }
}
```

### 7.2 댓글 페이징 최적화
```kotlin
@Service
class CommentPaginationService {
    
    fun getCommentsWithReplies(
        postId: String,
        page: Int,
        size: Int,
        maxRepliesPerComment: Int = 3
    ): Page<CommentWithReplies> {
        
        // 루트 댓글들을 페이징으로 조회
        val rootComments = commentRepository.findRootCommentsByPostId(
            postId, 
            PageRequest.of(page, size)
        )
        
        val commentsWithReplies = rootComments.content.map { comment ->
            // 각 댓글의 최신 답글들 조회
            val latestReplies = commentRepository.findTopReplies(
                comment.id, 
                maxRepliesPerComment
            )
            
            CommentWithReplies(
                comment = comment,
                replies = latestReplies,
                totalReplies = comment.replyCount,
                hasMoreReplies = comment.replyCount > latestReplies.size
            )
        }
        
        return PageImpl(
            commentsWithReplies,
            PageRequest.of(page, size),
            rootComments.totalElements
        )
    }
}
```

## 8. 자동 모더레이션

### 8.1 컨텐츠 필터링
```kotlin
@Service
class CommentModerationService {
    
    private val profanityFilter = ProfanityFilter()
    private val spamDetector = SpamDetector()
    
    fun moderateComment(comment: Comment): ModerationResult {
        val flags = mutableListOf<String>()
        
        // 욕설 탐지
        if (profanityFilter.containsProfanity(comment.content)) {
            flags.add("PROFANITY_DETECTED")
        }
        
        // 스팸 탐지
        if (spamDetector.isSpam(comment.content)) {
            flags.add("SPAM_DETECTED")
        }
        
        // 외부 링크 탐지
        if (containsExternalLinks(comment.content)) {
            flags.add("EXTERNAL_LINKS")
        }
        
        // 반복 게시 탐지
        if (isDuplicateComment(comment.authorId, comment.content)) {
            flags.add("DUPLICATE_CONTENT")
        }
        
        val severity = calculateSeverity(flags)
        val action = determineAction(severity)
        
        return ModerationResult(
            flags = flags,
            severity = severity,
            action = action,
            confidence = calculateConfidence(flags)
        )
    }
    
    private fun calculateSeverity(flags: List<String>): ModerationSeverity {
        return when {
            flags.contains("PROFANITY_DETECTED") -> ModerationSeverity.HIGH
            flags.contains("SPAM_DETECTED") -> ModerationSeverity.MEDIUM
            flags.contains("EXTERNAL_LINKS") -> ModerationSeverity.LOW
            else -> ModerationSeverity.NONE
        }
    }
    
    private fun determineAction(severity: ModerationSeverity): ModerationAction {
        return when (severity) {
            ModerationSeverity.HIGH -> ModerationAction.HIDE
            ModerationSeverity.MEDIUM -> ModerationAction.PENDING_REVIEW
            ModerationSeverity.LOW -> ModerationAction.FLAG_FOR_REVIEW
            ModerationSeverity.NONE -> ModerationAction.APPROVE
        }
    }
}
```

### 8.2 감정 분석
```kotlin
@Service
class CommentSentimentService {
    
    fun analyzeSentiment(content: String): SentimentResult {
        // 간단한 감정 분석 (실제로는 ML 모델 사용)
        val positiveWords = listOf("좋", "감사", "훌륭", "유익", "도움")
        val negativeWords = listOf("싫", "나쁘", "별로", "실망", "화남")
        
        val words = content.split(Regex("\\s+"))
        
        val positiveCount = words.count { word ->
            positiveWords.any { positive -> word.contains(positive) }
        }
        
        val negativeCount = words.count { word ->
            negativeWords.any { negative -> word.contains(negative) }
        }
        
        val totalSentimentWords = positiveCount + negativeCount
        
        val score = if (totalSentimentWords == 0) {
            0.0
        } else {
            (positiveCount - negativeCount).toDouble() / totalSentimentWords
        }
        
        val sentiment = when {
            score > 0.3 -> SentimentType.POSITIVE
            score < -0.3 -> SentimentType.NEGATIVE
            else -> SentimentType.NEUTRAL
        }
        
        return SentimentResult(
            sentiment = sentiment,
            score = score,
            confidence = calculateConfidence(totalSentimentWords, words.size)
        )
    }
}
```

## 9. 실시간 기능

### 9.1 실시간 댓글 알림
```kotlin
@Service
class CommentNotificationService(
    private val messagingTemplate: SimpMessagingTemplate,
    private val notificationService: NotificationService
) {
    
    fun notifyNewComment(comment: Comment, post: Post) {
        // 실시간 WebSocket 알림
        messagingTemplate.convertAndSend(
            "/topic/posts/${comment.postId}/comments",
            CommentEvent(
                type = "COMMENT_ADDED",
                comment = comment
            )
        )
        
        // 게시글 작성자에게 알림
        if (comment.authorId != post.authorId) {
            notificationService.send(
                userId = post.authorId,
                type = NotificationType.NEW_COMMENT,
                channels = listOf(NotificationChannel.PUSH, NotificationChannel.EMAIL),
                data = mapOf(
                    "postTitle" to post.title,
                    "commenterName" to getUserName(comment.authorId),
                    "commentPreview" to comment.content.take(100)
                )
            )
        }
        
        // 언급된 사용자들에게 알림
        comment.mentions.forEach { mention ->
            notificationService.send(
                userId = mention.userId,
                type = NotificationType.COMMENT_MENTION,
                channels = listOf(NotificationChannel.PUSH),
                data = mapOf(
                    "postTitle" to post.title,
                    "commenterName" to getUserName(comment.authorId),
                    "commentContent" to comment.content
                )
            )
        }
    }
    
    fun notifyCommentReaction(commentId: String, reaction: CommentReactionType, userId: String) {
        val comment = commentRepository.findById(commentId)
        
        // 실시간 반응 업데이트
        messagingTemplate.convertAndSend(
            "/topic/comments/$commentId/reactions",
            ReactionEvent(
                type = "REACTION_UPDATED",
                commentId = commentId,
                reactionType = reaction,
                userId = userId,
                newCount = getReactionCount(commentId, reaction)
            )
        )
        
        // 댓글 작성자에게 알림 (자신의 반응 제외)
        if (comment.authorId != userId) {
            notificationService.send(
                userId = comment.authorId,
                type = NotificationType.COMMENT_REACTION,
                channels = listOf(NotificationChannel.PUSH),
                data = mapOf(
                    "reactionType" to reaction.name,
                    "reactorName" to getUserName(userId),
                    "commentPreview" to comment.content.take(50)
                )
            )
        }
    }
}
```

## 10. 검색 및 분석

### 10.1 Elasticsearch 인덱싱
```kotlin
@Service
class CommentSearchService(
    private val elasticsearchClient: ElasticsearchClient
) {
    
    fun indexComment(comment: Comment) {
        val document = CommentDocument(
            id = comment.id,
            postId = comment.postId,
            authorId = comment.authorId,
            content = comment.content,
            level = comment.level,
            reactions = comment.reactions,
            sentiment = analyzeSentiment(comment.content),
            createdAt = comment.createdAt,
            isAnonymous = comment.isAnonymous
        )
        
        elasticsearchClient.index(
            IndexRequest.of { i ->
                i.index("comments")
                    .id(comment.id)
                    .document(document)
            }
        )
    }
    
    fun searchComments(query: CommentSearchQuery): CommentSearchResult {
        val searchRequest = SearchRequest.of { s ->
            s.index("comments")
                .query { q ->
                    q.bool { b ->
                        if (query.keyword.isNotEmpty()) {
                            b.must { m ->
                                m.match { match ->
                                    match.field("content").query(query.keyword)
                                }
                            }
                        }
                        
                        query.postId?.let { postId ->
                            b.filter { f -> f.term { t -> t.field("postId").value(postId) } }
                        }
                        
                        query.authorId?.let { authorId ->
                            b.filter { f -> f.term { t -> t.field("authorId").value(authorId) } }
                        }
                        
                        // 감정 필터
                        query.sentiment?.let { sentiment ->
                            b.filter { f -> f.term { t -> t.field("sentiment").value(sentiment) } }
                        }
                    }
                }
                .highlight { h ->
                    h.fields("content") { f ->
                        f.preTags("<em>").postTags("</em>")
                            .fragmentSize(150)
                    }
                }
                .sort { sort ->
                    when (query.sort) {
                        CommentSortType.LATEST -> sort.field { it.field("createdAt").order(SortOrder.Desc) }
                        CommentSortType.OLDEST -> sort.field { it.field("createdAt").order(SortOrder.Asc) }
                        CommentSortType.MOST_LIKED -> sort.field { it.field("reactions.likes").order(SortOrder.Desc) }
                        CommentSortType.RELEVANCE -> sort.score { it.order(SortOrder.Desc) }
                    }
                }
                .from(query.page * query.size)
                .size(query.size)
        }
        
        return elasticsearchClient.search(searchRequest, CommentDocument::class.java)
    }
}
```

## 11. 성능 최적화

### 11.1 캐싱 전략
```kotlin
@Service
class CommentCacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    
    fun getCachedComments(postId: String, page: Int, size: Int): List<Comment>? {
        val cacheKey = "comments:$postId:$page:$size"
        return redisTemplate.opsForValue().get(cacheKey) as? List<Comment>
    }
    
    fun cacheComments(
        postId: String, 
        page: Int, 
        size: Int, 
        comments: List<Comment>,
        ttl: Duration = Duration.ofMinutes(10)
    ) {
        val cacheKey = "comments:$postId:$page:$size"
        redisTemplate.opsForValue().set(cacheKey, comments, ttl)
    }
    
    fun getCachedReactionCount(commentId: String): CommentReactions? {
        return redisTemplate.opsForValue().get("reactions:$commentId") as? CommentReactions
    }
    
    fun updateReactionCount(commentId: String, reactions: CommentReactions) {
        redisTemplate.opsForValue().set("reactions:$commentId", reactions, Duration.ofHours(1))
    }
    
    fun invalidateCommentCache(postId: String) {
        val pattern = "comments:$postId:*"
        val keys = redisTemplate.keys(pattern)
        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        }
    }
}
```

### 11.2 반응 수 집계 최적화
```kotlin
@Service
class ReactionAggregationService {
    private val reactionBuffer = ConcurrentHashMap<String, ConcurrentHashMap<CommentReactionType, AtomicLong>>()
    
    fun incrementReaction(commentId: String, reactionType: CommentReactionType) {
        reactionBuffer
            .computeIfAbsent(commentId) { ConcurrentHashMap() }
            .computeIfAbsent(reactionType) { AtomicLong(0) }
            .incrementAndGet()
    }
    
    fun decrementReaction(commentId: String, reactionType: CommentReactionType) {
        reactionBuffer
            .computeIfAbsent(commentId) { ConcurrentHashMap() }
            .computeIfAbsent(reactionType) { AtomicLong(0) }
            .decrementAndGet()
    }
    
    @Scheduled(fixedDelay = 15000) // 15초마다
    fun flushReactionCounts() {
        if (reactionBuffer.isEmpty()) return
        
        val currentBuffer = reactionBuffer.toMap()
        reactionBuffer.clear()
        
        currentBuffer.forEach { (commentId, reactions) ->
            val updates = mutableMapOf<String, Long>()
            
            reactions.forEach { (type, count) ->
                val countValue = count.get()
                if (countValue != 0L) {
                    updates["reactions.${type.name.lowercase()}"] = countValue
                }
            }
            
            if (updates.isNotEmpty()) {
                commentRepository.incrementReactions(commentId, updates)
            }
        }
    }
}
```

## 12. 모니터링

### 12.1 비즈니스 메트릭
- 일일 댓글 수
- 평균 댓글 길이
- 댓글 참여율
- 감정 분포
- 신고율

### 12.2 기술 메트릭
- 댓글 로딩 시간
- 검색 성능
- 캐시 히트율
- 실시간 알림 지연시간
- 모더레이션 정확도 