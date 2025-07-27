# Post Service (게시글 서비스)

## 1. 서비스 개요

### 1.1 목적
Post Service는 시그나이트 플랫폼의 모든 게시글을 관리합니다. 다양한 형태의 게시글 작성, 수정, 삭제와 함께 첨부파일, 이미지, 링크 미리보기 등 풍부한 컨텐츠를 지원합니다.

### 1.2 주요 책임
- 게시글 CRUD 작업
- 첨부파일 및 이미지 관리
- 게시글 버전 관리
- 게시글 검색 및 필터링
- 게시글 통계 및 분석
- 게시글 자동 태깅

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: MongoDB (메인), PostgreSQL (메타데이터)
cache: Redis
storage: AWS S3 (첨부파일)
search: Elasticsearch
nlp: OpenAI API (자동 태깅)
```

## 3. API 설계

### 3.1 게시글 관리 API

#### POST /api/v1/posts
게시글 작성
```json
// Request
{
  "categoryId": "cat-uuid",
  "sigId": "sig-uuid", // SIG 게시글인 경우
  "title": "ChatGPT API 활용 가이드",
  "content": "# 개요\n\nChatGPT API를 활용하는 방법에 대해 설명드립니다...",
  "contentType": "MARKDOWN", // MARKDOWN, HTML, PLAIN_TEXT
  "excerpt": "ChatGPT API 활용법 요약", // 선택적
  "tags": ["AI", "ChatGPT", "API"],
  "attachments": [
    {
      "fileId": "file-uuid-1",
      "description": "예제 코드",
      "displayOrder": 1
    }
  ],
  "settings": {
    "allowComments": true,
    "allowReactions": true,
    "isPinned": false,
    "isAnonymous": false,
    "requireApproval": false,
    "publishAt": "2024-11-15T14:00:00Z", // 예약 발행
    "expiresAt": null // 만료일 (선택적)
  },
  "metadata": {
    "readingTime": 5,
    "difficulty": "INTERMEDIATE",
    "language": "ko"
  }
}

// Response
{
  "id": "post-uuid",
  "slug": "chatgpt-api-guide",
  "status": "PUBLISHED", // DRAFT, PUBLISHED, SCHEDULED, ARCHIVED
  "createdAt": "2024-11-15T10:00:00Z",
  "publishedAt": "2024-11-15T14:00:00Z",
  "url": "/posts/chatgpt-api-guide"
}
```

#### GET /api/v1/posts/{postId}
게시글 상세 조회
```json
// Response
{
  "id": "post-uuid",
  "slug": "chatgpt-api-guide",
  "category": {
    "id": "cat-uuid",
    "name": "스터디 자료",
    "path": "AI 연구 모임 > 스터디 자료"
  },
  "sig": {
    "id": "sig-uuid",
    "name": "AI 연구 모임"
  },
  "author": {
    "id": "user-uuid",
    "name": "홍길동",
    "nickname": "길동이",
    "profileImage": "https://cdn.signight.com/profiles/...",
    "memberType": "MENSA_MEMBER"
  },
  "title": "ChatGPT API 활용 가이드",
  "content": "# 개요\n\nChatGPT API를 활용하는 방법에 대해 설명드립니다...",
  "contentHtml": "<h1>개요</h1><p>ChatGPT API를 활용하는 방법...</p>",
  "excerpt": "ChatGPT API 활용법 요약",
  "tags": ["AI", "ChatGPT", "API"],
  "attachments": [
    {
      "id": "attachment-uuid",
      "fileName": "chatgpt_example.py",
      "fileSize": 2048,
      "mimeType": "text/x-python",
      "downloadUrl": "https://cdn.signight.com/files/...",
      "thumbnailUrl": null,
      "description": "예제 코드"
    }
  ],
  "statistics": {
    "views": 245,
    "likes": 18,
    "comments": 5,
    "bookmarks": 12,
    "shares": 3
  },
  "userInteraction": {
    "hasViewed": true,
    "hasLiked": false,
    "hasBookmarked": true,
    "lastViewedAt": "2024-11-15T15:30:00Z"
  },
  "status": "PUBLISHED",
  "version": 2,
  "createdAt": "2024-11-15T10:00:00Z",
  "updatedAt": "2024-11-15T11:30:00Z",
  "publishedAt": "2024-11-15T14:00:00Z"
}
```

#### PUT /api/v1/posts/{postId}
게시글 수정
```json
// Request
{
  "title": "ChatGPT API 완전 활용 가이드",
  "content": "# 개요\n\n업데이트된 ChatGPT API 활용법...",
  "tags": ["AI", "ChatGPT", "API", "완벽가이드"],
  "settings": {
    "isPinned": true
  },
  "versionNote": "내용 보강 및 최신 API 정보 추가"
}

// Response
{
  "success": true,
  "version": 3,
  "updatedAt": "2024-11-16T09:00:00Z",
  "changes": [
    "title",
    "content",
    "tags",
    "settings.isPinned"
  ]
}
```

#### DELETE /api/v1/posts/{postId}
게시글 삭제
```json
// Query Parameters
?permanent=false&reason=spam

// Response
{
  "success": true,
  "deletedAt": "2024-11-16T10:00:00Z",
  "isPermanent": false
}
```

### 3.2 게시글 목록 API

#### GET /api/v1/posts
게시글 목록 조회
```json
// Query Parameters
?categoryId=cat-uuid&sigId=sig-uuid&authorId=user-uuid&status=PUBLISHED&tags=AI,ChatGPT&sort=LATEST&page=0&size=20

// Response
{
  "content": [
    {
      "id": "post-uuid",
      "title": "ChatGPT API 활용 가이드",
      "excerpt": "ChatGPT API 활용법 요약",
      "author": {
        "name": "홍길동",
        "profileImage": "url"
      },
      "category": {
        "name": "스터디 자료"
      },
      "tags": ["AI", "ChatGPT"],
      "statistics": {
        "views": 245,
        "likes": 18,
        "comments": 5
      },
      "thumbnailUrl": "https://cdn.signight.com/thumbnails/...",
      "publishedAt": "2024-11-15T14:00:00Z",
      "readingTime": 5
    }
  ],
  "totalElements": 156,
  "totalPages": 8,
  "number": 0,
  "size": 20,
  "sort": {
    "field": "publishedAt",
    "direction": "DESC"
  }
}
```

#### GET /api/v1/posts/trending
인기 게시글
```json
// Query Parameters
?period=WEEK&categoryId=cat-uuid&limit=10

// Response
{
  "posts": [
    {
      "id": "post-uuid",
      "title": "ChatGPT API 활용 가이드",
      "author": {
        "name": "홍길동"
      },
      "trendScore": 95.5,
      "rank": 1,
      "rankChange": "+3",
      "statistics": {
        "views": 1250,
        "likes": 89,
        "comments": 23
      }
    }
  ],
  "period": {
    "start": "2024-11-08T00:00:00Z",
    "end": "2024-11-15T00:00:00Z"
  }
}
```

### 3.3 게시글 검색 API

#### GET /api/v1/posts/search
게시글 검색
```json
// Query Parameters
?query=ChatGPT&categoryId=cat-uuid&authorId=user-uuid&tags=AI&dateFrom=2024-11-01&dateTo=2024-11-30&sort=RELEVANCE

// Response
{
  "results": [
    {
      "post": {
        "id": "post-uuid",
        "title": "ChatGPT API 활용 가이드",
        "excerpt": "ChatGPT API 활용법 요약",
        "author": {
          "name": "홍길동"
        },
        "publishedAt": "2024-11-15T14:00:00Z"
      },
      "highlights": {
        "title": "<em>ChatGPT</em> API 활용 가이드",
        "content": "<em>ChatGPT</em>를 활용하는 방법에 대해..."
      },
      "relevanceScore": 0.95
    }
  ],
  "totalResults": 23,
  "searchTime": 45,
  "suggestions": ["ChatGPT API", "ChatGPT 활용법"],
  "facets": {
    "categories": [
      {
        "categoryId": "cat-uuid",
        "name": "스터디 자료",
        "count": 15
      }
    ],
    "tags": [
      {
        "name": "AI",
        "count": 18
      },
      {
        "name": "API",
        "count": 12
      }
    ]
  }
}
```

### 3.4 게시글 반응 API

#### POST /api/v1/posts/{postId}/reactions
게시글 반응 추가
```json
// Request
{
  "type": "LIKE" // LIKE, LOVE, HELPFUL, INSIGHTFUL
}

// Response
{
  "success": true,
  "reactions": {
    "LIKE": 19,
    "LOVE": 5,
    "HELPFUL": 8,
    "INSIGHTFUL": 3
  },
  "userReaction": "LIKE"
}
```

#### GET /api/v1/posts/{postId}/reactions
게시글 반응 목록
```json
// Response
{
  "reactions": [
    {
      "type": "LIKE",
      "count": 19,
      "users": [
        {
          "userId": "user-uuid",
          "name": "김철수",
          "reactedAt": "2024-11-15T15:00:00Z"
        }
      ]
    }
  ],
  "totalReactions": 35,
  "userReaction": "LIKE"
}
```

### 3.5 게시글 버전 관리 API

#### GET /api/v1/posts/{postId}/versions
게시글 버전 이력
```json
// Response
{
  "versions": [
    {
      "version": 3,
      "title": "ChatGPT API 완전 활용 가이드",
      "editedBy": {
        "name": "홍길동"
      },
      "versionNote": "내용 보강 및 최신 API 정보 추가",
      "changes": ["title", "content", "tags"],
      "createdAt": "2024-11-16T09:00:00Z",
      "isCurrent": true
    },
    {
      "version": 2,
      "title": "ChatGPT API 활용 가이드",
      "editedBy": {
        "name": "홍길동"
      },
      "versionNote": "오타 수정",
      "changes": ["content"],
      "createdAt": "2024-11-15T11:30:00Z",
      "isCurrent": false
    }
  ]
}
```

#### GET /api/v1/posts/{postId}/versions/{version}/diff
버전 간 비교
```json
// Response
{
  "fromVersion": 2,
  "toVersion": 3,
  "diff": {
    "title": {
      "from": "ChatGPT API 활용 가이드",
      "to": "ChatGPT API 완전 활용 가이드",
      "type": "MODIFIED"
    },
    "content": {
      "additions": [
        {
          "line": 15,
          "content": "## 고급 활용법"
        }
      ],
      "deletions": [
        {
          "line": 10,
          "content": "기본적인 사용법"
        }
      ],
      "modifications": [
        {
          "line": 5,
          "from": "간단한 예제",
          "to": "상세한 예제"
        }
      ]
    }
  }
}
```

## 4. 데이터베이스 설계

### 4.1 MongoDB 컬렉션

#### posts
```javascript
{
  "_id": ObjectId("..."),
  "slug": "chatgpt-api-guide",
  "categoryId": "cat-uuid",
  "sigId": "sig-uuid",
  "authorId": "user-uuid",
  "title": "ChatGPT API 활용 가이드",
  "content": "# 개요\n\nChatGPT API를 활용하는 방법...",
  "contentHtml": "<h1>개요</h1><p>ChatGPT API를 활용하는 방법...</p>",
  "contentType": "MARKDOWN",
  "excerpt": "ChatGPT API 활용법 요약",
  "tags": ["AI", "ChatGPT", "API"],
  "attachments": [
    {
      "id": "attachment-uuid",
      "fileId": "file-uuid",
      "fileName": "example.py",
      "fileSize": 2048,
      "mimeType": "text/x-python",
      "description": "예제 코드",
      "displayOrder": 1
    }
  ],
  "statistics": {
    "views": 245,
    "likes": 18,
    "comments": 5,
    "bookmarks": 12,
    "shares": 3
  },
  "settings": {
    "allowComments": true,
    "allowReactions": true,
    "isPinned": false,
    "isAnonymous": false,
    "requireApproval": false
  },
  "metadata": {
    "readingTime": 5,
    "difficulty": "INTERMEDIATE",
    "language": "ko",
    "thumbnailUrl": "https://cdn.signight.com/thumbnails/...",
    "autoTags": ["machine-learning", "programming"]
  },
  "status": "PUBLISHED",
  "version": 3,
  "publishedAt": ISODate("2024-11-15T14:00:00Z"),
  "scheduledAt": null,
  "expiresAt": null,
  "createdAt": ISODate("2024-11-15T10:00:00Z"),
  "updatedAt": ISODate("2024-11-16T09:00:00Z"),
  "deletedAt": null
}
```

#### post_versions
```javascript
{
  "_id": ObjectId("..."),
  "postId": ObjectId("..."),
  "version": 2,
  "title": "ChatGPT API 활용 가이드",
  "content": "이전 버전의 내용...",
  "contentHtml": "<p>이전 버전의 내용...</p>",
  "tags": ["AI", "ChatGPT"],
  "versionNote": "오타 수정",
  "changes": ["content"],
  "editedBy": "user-uuid",
  "createdAt": ISODate("2024-11-15T11:30:00Z")
}
```

### 4.2 PostgreSQL 테이블

#### post_interactions
```sql
CREATE TABLE post_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id VARCHAR(24) NOT NULL, -- MongoDB ObjectId
    user_id UUID NOT NULL,
    interaction_type VARCHAR(20) NOT NULL, -- VIEW, LIKE, BOOKMARK, SHARE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

CREATE INDEX idx_interactions_post_id ON post_interactions(post_id);
CREATE INDEX idx_interactions_user_id ON post_interactions(user_id);
CREATE INDEX idx_interactions_type ON post_interactions(interaction_type);
CREATE UNIQUE INDEX idx_interactions_unique ON post_interactions(post_id, user_id, interaction_type) 
WHERE interaction_type IN ('LIKE', 'BOOKMARK');
```

#### post_reactions
```sql
CREATE TABLE post_reactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id VARCHAR(24) NOT NULL,
    user_id UUID NOT NULL,
    reaction_type VARCHAR(20) NOT NULL, -- LIKE, LOVE, HELPFUL, INSIGHTFUL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, user_id)
);

CREATE INDEX idx_reactions_post_id ON post_reactions(post_id);
CREATE INDEX idx_reactions_type ON post_reactions(reaction_type);
```

#### post_analytics
```sql
CREATE TABLE post_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id VARCHAR(24) NOT NULL,
    analytics_date DATE NOT NULL,
    views INTEGER DEFAULT 0,
    unique_views INTEGER DEFAULT 0,
    likes INTEGER DEFAULT 0,
    comments INTEGER DEFAULT 0,
    shares INTEGER DEFAULT 0,
    avg_reading_time DECIMAL(5,2) DEFAULT 0,
    bounce_rate DECIMAL(5,4) DEFAULT 0,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, analytics_date)
);

CREATE INDEX idx_analytics_post_date ON post_analytics(post_id, analytics_date);
```

## 5. 도메인 모델

### 5.1 게시글 상태
```kotlin
enum class PostStatus {
    DRAFT,      // 초안
    PUBLISHED,  // 발행됨
    SCHEDULED,  // 예약됨
    ARCHIVED,   // 보관됨
    DELETED     // 삭제됨
}
```

### 5.2 컨텐츠 유형
```kotlin
enum class ContentType {
    PLAIN_TEXT,  // 일반 텍스트
    MARKDOWN,    // 마크다운
    HTML         // HTML
}
```

### 5.3 반응 유형
```kotlin
enum class ReactionType {
    LIKE,        // 좋아요
    LOVE,        // 사랑해요
    HELPFUL,     // 도움돼요
    INSIGHTFUL   // 통찰력있어요
}
```

### 5.4 게시글 엔티티
```kotlin
@Document(collection = "posts")
data class Post(
    @Id
    val id: String = ObjectId().toString(),
    
    val slug: String,
    val categoryId: String,
    val sigId: String? = null,
    val authorId: String,
    
    var title: String,
    var content: String,
    var contentHtml: String,
    val contentType: ContentType = ContentType.MARKDOWN,
    var excerpt: String? = null,
    
    var tags: List<String> = emptyList(),
    var attachments: List<PostAttachment> = emptyList(),
    
    val statistics: PostStatistics = PostStatistics(),
    val settings: PostSettings = PostSettings(),
    val metadata: PostMetadata = PostMetadata(),
    
    var status: PostStatus = PostStatus.DRAFT,
    var version: Int = 1,
    
    val publishedAt: LocalDateTime? = null,
    val scheduledAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null,
    
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    var deletedAt: LocalDateTime? = null
) {
    fun isPublished(): Boolean = status == PostStatus.PUBLISHED
    fun isScheduled(): Boolean = status == PostStatus.SCHEDULED
    fun isExpired(): Boolean = expiresAt?.isBefore(LocalDateTime.now()) == true
    
    fun generateSlug(): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9가-힣\\s]"), "")
            .replace(Regex("\\s+"), "-")
            .take(50)
    }
}

data class PostAttachment(
    val id: String,
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val description: String? = null,
    val displayOrder: Int = 0,
    val thumbnailUrl: String? = null
)

data class PostStatistics(
    var views: Long = 0,
    var likes: Long = 0,
    var comments: Long = 0,
    var bookmarks: Long = 0,
    var shares: Long = 0
)

data class PostSettings(
    val allowComments: Boolean = true,
    val allowReactions: Boolean = true,
    var isPinned: Boolean = false,
    val isAnonymous: Boolean = false,
    val requireApproval: Boolean = false
)

data class PostMetadata(
    val readingTime: Int? = null,
    val difficulty: String? = null,
    val language: String = "ko",
    val thumbnailUrl: String? = null,
    val autoTags: List<String> = emptyList()
)
```

## 6. 컨텐츠 처리

### 6.1 마크다운 처리
```kotlin
@Service
class MarkdownProcessor {
    private val parser = Parser.builder()
        .extensions(listOf(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            TaskListItemsExtension.create(),
            AutolinkExtension.create()
        ))
        .build()
        
    private val htmlRenderer = HtmlRenderer.builder()
        .extensions(listOf(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            TaskListItemsExtension.create()
        ))
        .attributeProviderFactory { context ->
            AttributeProvider { node, tagName, attributes ->
                // 외부 링크에 target="_blank" 추가
                if (tagName == "a" && attributes["href"]?.startsWith("http") == true) {
                    attributes["target"] = "_blank"
                    attributes["rel"] = "noopener noreferrer"
                }
                
                // 코드 블록에 syntax highlighting 클래스 추가
                if (tagName == "code" && node.parent is FencedCodeBlock) {
                    val language = (node.parent as FencedCodeBlock).info
                    if (language.isNotEmpty()) {
                        attributes["class"] = "language-$language"
                    }
                }
            }
        }
        .build()
    
    fun processMarkdown(content: String): ProcessedContent {
        val document = parser.parse(content)
        val html = htmlRenderer.render(document)
        
        return ProcessedContent(
            html = html,
            readingTime = calculateReadingTime(content),
            headings = extractHeadings(document),
            images = extractImages(document),
            links = extractLinks(document)
        )
    }
    
    private fun calculateReadingTime(content: String): Int {
        val wordCount = content.split(Regex("\\s+")).size
        return (wordCount / 200).coerceAtLeast(1) // 분당 200단어 기준
    }
}
```

### 6.2 자동 태깅
```kotlin
@Service
class AutoTaggingService(
    private val openAiClient: OpenAiClient
) {
    suspend fun generateTags(title: String, content: String): List<String> {
        val prompt = """
        다음 게시글의 제목과 내용을 분석하여 적절한 태그를 추천해주세요.
        태그는 한국어로 5개 이하로 제안하고, 쉼표로 구분해주세요.
        
        제목: $title
        내용: ${content.take(500)}...
        
        태그:
        """.trimIndent()
        
        val response = openAiClient.createCompletion(
            CompletionRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    ChatMessage(role = "user", content = prompt)
                ),
                maxTokens = 100,
                temperature = 0.3
            )
        )
        
        return response.choices.first().message.content
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    
    fun extractKeywords(content: String): List<String> {
        // 간단한 키워드 추출 로직
        val words = content.lowercase()
            .replace(Regex("[^a-z0-9가-힣\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }
            
        return words.groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(10)
            .map { it.first }
    }
}
```

## 7. 검색 및 분석

### 7.1 Elasticsearch 인덱싱
```kotlin
@Service
class PostSearchService(
    private val elasticsearchClient: ElasticsearchClient
) {
    fun indexPost(post: Post) {
        val document = PostDocument(
            id = post.id,
            title = post.title,
            content = post.content,
            excerpt = post.excerpt,
            tags = post.tags,
            categoryId = post.categoryId,
            sigId = post.sigId,
            authorId = post.authorId,
            status = post.status.name,
            statistics = post.statistics,
            publishedAt = post.publishedAt,
            language = post.metadata.language
        )
        
        elasticsearchClient.index(
            IndexRequest.of { i ->
                i.index("posts")
                    .id(post.id)
                    .document(document)
            }
        )
    }
    
    fun searchPosts(query: PostSearchQuery): PostSearchResult {
        val searchRequest = SearchRequest.of { s ->
            s.index("posts")
                .query { q ->
                    q.bool { b ->
                        // 메인 검색 쿼리
                        if (query.keyword.isNotEmpty()) {
                            b.must { m ->
                                m.multiMatch { mm ->
                                    mm.query(query.keyword)
                                        .fields("title^3", "content^2", "excerpt^2", "tags^1.5")
                                        .type(TextQueryType.BestFields)
                                        .fuzziness("AUTO")
                                }
                            }
                        }
                        
                        // 필터
                        b.filter { f -> f.term { t -> t.field("status").value("PUBLISHED") } }
                        
                        query.categoryId?.let { categoryId ->
                            b.filter { f -> f.term { t -> t.field("categoryId").value(categoryId) } }
                        }
                        
                        query.sigId?.let { sigId ->
                            b.filter { f -> f.term { t -> t.field("sigId").value(sigId) } }
                        }
                        
                        query.tags?.let { tags ->
                            b.filter { f ->
                                f.terms { t ->
                                    t.field("tags").terms { terms ->
                                        terms.value(tags.map { FieldValue.of(it) })
                                    }
                                }
                            }
                        }
                        
                        // 날짜 범위
                        if (query.dateFrom != null || query.dateTo != null) {
                            b.filter { f ->
                                f.range { r ->
                                    val range = r.field("publishedAt")
                                    query.dateFrom?.let { range.gte(JsonData.of(it)) }
                                    query.dateTo?.let { range.lte(JsonData.of(it)) }
                                    range
                                }
                            }
                        }
                    }
                }
                .highlight { h ->
                    h.fields("title") { f -> f.preTags("<em>").postTags("</em>") }
                        .fields("content") { f -> 
                            f.preTags("<em>").postTags("</em>")
                                .fragmentSize(200)
                                .numberOfFragments(3)
                        }
                }
                .aggregations("categories") { a ->
                    a.terms { t -> t.field("categoryId").size(10) }
                }
                .aggregations("tags") { a ->
                    a.terms { t -> t.field("tags").size(20) }
                }
                .sort { sort ->
                    when (query.sort) {
                        PostSortType.RELEVANCE -> sort.score { it.order(SortOrder.Desc) }
                        PostSortType.LATEST -> sort.field { it.field("publishedAt").order(SortOrder.Desc) }
                        PostSortType.POPULAR -> sort.field { it.field("statistics.views").order(SortOrder.Desc) }
                        PostSortType.MOST_LIKED -> sort.field { it.field("statistics.likes").order(SortOrder.Desc) }
                    }
                }
                .from(query.page * query.size)
                .size(query.size)
        }
        
        return elasticsearchClient.search(searchRequest, PostDocument::class.java)
    }
}
```

## 8. 성능 최적화

### 8.1 캐싱 전략
```kotlin
@Service
class PostCacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    fun getCachedPost(postId: String): Post? {
        return redisTemplate.opsForValue().get("post:$postId") as? Post
    }
    
    fun cachePost(post: Post, ttl: Duration = Duration.ofHours(1)) {
        redisTemplate.opsForValue().set("post:${post.id}", post, ttl)
    }
    
    fun getCachedPostList(cacheKey: String): List<Post>? {
        return redisTemplate.opsForValue().get("posts:$cacheKey") as? List<Post>
    }
    
    fun cachePostList(cacheKey: String, posts: List<Post>, ttl: Duration = Duration.ofMinutes(15)) {
        redisTemplate.opsForValue().set("posts:$cacheKey", posts, ttl)
    }
    
    fun invalidatePostCache(postId: String) {
        redisTemplate.delete("post:$postId")
        
        // 관련 목록 캐시도 무효화
        val patterns = listOf(
            "posts:category:*",
            "posts:sig:*",
            "posts:trending:*",
            "posts:recent:*"
        )
        
        patterns.forEach { pattern ->
            val keys = redisTemplate.keys(pattern)
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys)
            }
        }
    }
}
```

### 8.2 조회수 최적화
```kotlin
@Service
class ViewCountService {
    private val viewCounts = ConcurrentHashMap<String, AtomicLong>()
    private val userViews = ConcurrentHashMap<String, MutableSet<String>>() // userId -> postIds
    
    fun incrementView(postId: String, userId: String?) {
        // 동일 사용자의 중복 조회 방지
        userId?.let { uid ->
            val userViewSet = userViews.computeIfAbsent(uid) { ConcurrentHashMap.newKeySet() }
            if (userViewSet.contains(postId)) {
                return
            }
            userViewSet.add(postId)
        }
        
        viewCounts.computeIfAbsent(postId) { AtomicLong(0) }.incrementAndGet()
    }
    
    @Scheduled(fixedDelay = 30000) // 30초마다
    fun flushViewCounts() {
        if (viewCounts.isEmpty()) return
        
        val batch = viewCounts.toMap()
        viewCounts.clear()
        
        // MongoDB 배치 업데이트
        val operations = batch.map { (postId, count) ->
            UpdateOneModel<Post>(
                Filters.eq("_id", ObjectId(postId)),
                Updates.inc("statistics.views", count.toLong())
            )
        }
        
        if (operations.isNotEmpty()) {
            mongoTemplate.collection<Post>().bulkWrite(operations)
        }
        
        // 사용자별 조회 기록 정리 (1시간마다)
        if (LocalDateTime.now().minute == 0) {
            userViews.clear()
        }
    }
}
```

## 9. 컨텐츠 보안

### 9.1 XSS 방지
```kotlin
@Service
class ContentSanitizer {
    private val policy = PolicyFactory.newBuilder()
        .allowElements("p", "br", "strong", "em", "u", "h1", "h2", "h3", "h4", "h5", "h6")
        .allowElements("ul", "ol", "li", "blockquote", "pre", "code")
        .allowElements("a").allowAttributes("href", "title").onElements("a")
        .allowElements("img").allowAttributes("src", "alt", "title").onElements("img")
        .allowElements("table", "thead", "tbody", "tr", "th", "td")
        .requireRelNofollowOnLinks()
        .toFactory()
    
    fun sanitizeHtml(html: String): String {
        return policy.sanitize(html)
    }
    
    fun sanitizeMarkdown(markdown: String): String {
        // 위험한 HTML 태그 제거
        return markdown.replace(Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<iframe[^>]*>.*?</iframe>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("javascript:", RegexOption.IGNORE_CASE), "")
    }
}
```

## 10. 모니터링

### 10.1 비즈니스 메트릭
- 일일 게시글 수
- 평균 조회수
- 인기 태그
- 카테고리별 활동
- 사용자별 게시 빈도

### 10.2 기술 메트릭
- 검색 쿼리 성능
- 컨텐츠 처리 시간
- 캐시 히트율
- MongoDB 쿼리 성능
- 파일 업로드 성공률 