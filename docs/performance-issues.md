# 성능 최적화 포인트 분석

## 1. N+1 쿼리 문제 🔴

### 문제점
포스트 조회 시 연관 데이터를 개별적으로 조회하여 N+1 문제 발생:

```kotlin
// PostService.getPost - 각 연관 데이터를 개별 쿼리로 조회
.flatMap { post ->
    Mono.zip(
        post.toMono(),
        postRepository.findTagsByPostId(postId).collectList().toMono(), // 추가 쿼리
        userRepository.findById(post.userId!!).toMono(),                // 추가 쿼리  
        categoryRepository.findById(post.categoryId!!).toMono(),        // 추가 쿼리
    )
}
```

### 해결방안
```kotlin
// 조인 쿼리로 한 번에 데이터 조회
@Query("""
    SELECT p.*, u.username, u.imageUrl, c.name as categoryName,
           GROUP_CONCAT(t.name) as tagNames
    FROM post p
    LEFT JOIN user u ON p.userId = u.id  
    LEFT JOIN category c ON p.categoryId = c.id
    LEFT JOIN post_to_tag pt ON p.id = pt.postId
    LEFT JOIN tag t ON pt.tagId = t.id
    WHERE p.id = :postId
    GROUP BY p.id
""")
fun findPostWithAllDetails(postId: Int): Mono<PostWithDetailsProjection>

// 또는 분리된 쿼리 최적화
fun findPostsWithDetails(postIds: List<Int>): Flux<PostWithDetails> {
    val posts = postRepository.findAllById(postIds)
    val users = userRepository.findAllByIdIn(extractUserIds(postIds))  
    val categories = categoryRepository.findAllByIdIn(extractCategoryIds(postIds))
    val tags = tagRepository.findAllByPostIdIn(postIds)
    
    return Flux.zip(posts, users, categories, tags) { ... }
}
```

## 2. 캐시 최적화 부족 🟡

### 문제점
캐시가 일부 기능에만 적용되고 캐시 무효화 전략이 없음:

```kotlin
// UserCacheRepository.kt - 사용자만 캐시 적용
fun findByNameWithCaching(username: String): Mono<User> {
    return reactiveRedisTemplate.opsForValue().get(username)
        .switchIfEmpty(setUserMono)
}

// 포스트, 카테고리 등은 캐시 미적용
// 캐시 무효화 로직 없음
```

### 해결방안
```kotlin
// 계층별 캐시 전략
@Service
class PostCacheService {
    
    @Cacheable(value = "posts", key = "#postId")
    fun findPostById(postId: Int): Mono<Post> = postRepository.findById(postId)
    
    @Cacheable(value = "post-details", key = "#postId") 
    fun findPostWithDetails(postId: Int): Mono<PostDTO> = 
        postService.getPostWithAllDetails(postId)
    
    @CacheEvict(value = ["posts", "post-details"], key = "#postId")
    fun evictPostCache(postId: Int): Mono<Void> = Mono.empty()
    
    @CacheEvict(value = ["posts", "post-details"], allEntries = true)
    fun evictAllPostCache(): Mono<Void> = Mono.empty()
}

// 캐시 워밍업 전략
@EventListener(ApplicationReadyEvent::class)
fun warmUpCache() {
    postRepository.findTopViewedPosts(100)
        .flatMap { postCacheService.findPostById(it.id) }
        .subscribe()
}
```

## 3. 데이터베이스 쿼리 최적화 🟡

### 문제점
복잡한 쿼리에서 성능 최적화 부족:

```kotlin
// PostRepository.kt - 서브쿼리 중복 실행
@Query("""
    SELECT /* ... */,
    ( SELECT COUNT(comment.id) FROM comment WHERE comment.postId = post.id ) as commentcount,
    ( SELECT COUNT(post.id) FROM post WHERE post.categoryId = :categoryId ) as postcount,
    CEIL( ( SELECT COUNT(post.id) FROM post WHERE post.categoryId = :categoryId ) / 8 ) as last
    FROM post JOIN user ON user.id = post.userId 
    WHERE post.categoryId = :categoryId 
    LIMIT :limit OFFSET :offset
""")
fun findAllByCategoryId(categoryId: Int, offset: Int, limit: Int? = 8): Flux<PostInCategoryInDTO>
```

### 해결방안
```kotlin
// 서브쿼리 최적화 - 공통 테이블 식(CTE) 사용
@Query("""
    WITH category_stats AS (
        SELECT COUNT(*) as total_posts FROM post WHERE categoryId = :categoryId
    ),
    post_comments AS (
        SELECT postId, COUNT(*) as comment_count 
        FROM comment 
        GROUP BY postId
    )
    SELECT p.*, u.username, u.imageUrl,
           COALESCE(pc.comment_count, 0) as commentcount,
           cs.total_posts as postcount,
           CEIL(cs.total_posts / 8.0) as last
    FROM post p
    JOIN user u ON u.id = p.userId
    LEFT JOIN post_comments pc ON pc.postId = p.id
    CROSS JOIN category_stats cs
    WHERE p.categoryId = :categoryId
    ORDER BY p.createdAt DESC
    LIMIT :limit OFFSET :offset
""")
fun findAllByCategoryIdOptimized(categoryId: Int, offset: Int, limit: Int): Flux<PostInCategoryInDTO>

// 인덱스 추가 권장
/*
CREATE INDEX idx_post_category_created ON post(categoryId, createdAt);
CREATE INDEX idx_comment_post ON comment(postId);
CREATE INDEX idx_post_user ON post(userId);
*/
```

## 4. 페이징 성능 개선 🟡

### 문제점
OFFSET 기반 페이징으로 대용량 데이터에서 성능 저하:

```kotlin
// 큰 OFFSET 값에서 성능 저하
fun findAllByCategoryId(categoryId: Int, offset: Int, limit: Int? = 8)
```

### 해결방안
```kotlin
// 커서 기반 페이징 도입
data class CursorPage<T>(
    val content: List<T>,
    val nextCursor: String?,
    val hasNext: Boolean
)

@Query("""
    SELECT * FROM post 
    WHERE categoryId = :categoryId 
    AND (:cursor IS NULL OR createdAt < :cursor)
    ORDER BY createdAt DESC 
    LIMIT :size
""")
fun findByCategoryIdWithCursor(
    categoryId: Int, 
    cursor: LocalDateTime?, 
    size: Int
): Flux<Post>

// 서비스에서 커서 페이징 구현
fun getPostsByCursor(categoryId: Int, cursor: String?, size: Int): Mono<CursorPage<PostDTO>> {
    val cursorDate = cursor?.let { LocalDateTime.parse(it) }
    
    return postRepository.findByCategoryIdWithCursor(categoryId, cursorDate, size + 1)
        .collectList()
        .map { posts ->
            val hasNext = posts.size > size
            val content = if (hasNext) posts.dropLast(1) else posts
            val nextCursor = if (hasNext) posts.last().createdAt.toString() else null
            
            CursorPage(content.map { it.toDTO() }, nextCursor, hasNext)
        }
}
```

## 5. 연결 풀 최적화 🟡

### 문제점
R2DBC 연결 풀 설정이 기본값으로 되어 있어 최적화 필요:

```kotlin
// R2DBCConfig.kt - 기본 연결 풀 설정
@Bean
override fun connectionFactory(): ConnectionFactory {
    return MySqlConnectionFactory.from(
        MySqlConnectionConfiguration.builder()
            .host(url)
            .password(password)
            .port(port.toInt())
            .database(database)
            .username(username)
            .build()
    )
}
```

### 해결방안
```kotlin
@Bean
override fun connectionFactory(): ConnectionFactory {
    val config = MySqlConnectionConfiguration.builder()
        .host(url)
        .password(password)
        .port(port.toInt())
        .database(database)
        .username(username)
        .connectTimeout(Duration.ofSeconds(10))
        .sslMode(SslMode.REQUIRED)
        .build()
    
    return ConnectionPoolConfiguration.builder(MySqlConnectionFactory.from(config))
        .maxIdleTime(Duration.ofMinutes(30))
        .initialSize(5)
        .maxSize(20)
        .maxLifeTime(Duration.ofHours(1))
        .validationQuery("SELECT 1")
        .build()
        .let { ConnectionPools.newPool(it) }
}
```

## 6. Redis 성능 최적화 🟡

### 문제점
Redis 사용이 효율적이지 않음:

```kotlin
// UserCacheRepository.kt - 개별 키 조회만 사용
reactiveRedisTemplate.opsForValue().get(username)
```

### 해결방안
```kotlin
// 배치 처리 및 파이프라인 사용
class OptimizedCacheRepository {
    
    fun findUsersBatch(usernames: List<String>): Mono<Map<String, User>> {
        return reactiveRedisTemplate.opsForValue()
            .multiGet(usernames)
            .map { values ->
                usernames.zip(values).filter { it.second != null }
                    .associate { it.first to it.second!! }
            }
    }
    
    fun saveUsersBatch(users: Map<String, User>): Mono<Void> {
        return reactiveRedisTemplate.opsForValue()
            .multiSet(users)
            .then()
    }
    
    // 캐시 압축 (큰 객체용)
    fun saveCompressed(key: String, value: Any): Mono<Void> {
        val compressed = compressionService.compress(value)
        return reactiveRedisTemplate.opsForValue()
            .set(key, compressed, Duration.ofHours(1))
    }
}
```

## 7. 비동기 처리 최적화 🟡

### 문제점
순차적인 비동기 처리로 성능 저하:

```kotlin
// AuthHandler.comment - 순차 처리
.flatMap { userForm ->
    validationService.checkUsernameBoolean(userForm).toMono()
}.flatMap { result ->
    // 다음 단계...
}
```

### 해결방안
```kotlin
// 병렬 처리 최적화
fun processCommentRequest(userForm: UserFormDTO): Mono<ServerResponse> {
    val validationMono = validationService.checkUsernameBoolean(userForm)
    val userMono = if (userForm.username.isNullOrBlank()) {
        Mono.just(userForm.apply { 
            username = secretUser
            password = secretPassword 
        })
    } else {
        Mono.just(userForm)
    }
    
    return Mono.zip(validationMono, userMono)
        .flatMap { (isValid, form) ->
            if (isValid) {
                handleExistingUser(form)
            } else {
                handleNewUser(form)
            }
        }
        .flatMap { user ->
            generateTokenAndRespond(user)
        }
}

// WebFlux 최적화 설정
@Configuration
class WebFluxConfig : WebFluxConfigurer {
    
    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().maxInMemorySize(1024 * 1024) // 1MB
        configurer.defaultCodecs().enableLoggingRequestDetails(true)
    }
}
```

## 권장 성능 최적화 방안

1. **데이터베이스 최적화**
   - 인덱스 추가: `categoryId`, `userId`, `createdAt` 조합 인덱스
   - 쿼리 최적화: 서브쿼리를 JOIN으로 변경
   - 커서 기반 페이징 도입

2. **캐싱 전략**
   - 다층 캐시 구조: L1(로컬), L2(Redis)
   - 캐시 워밍업 전략 수립
   - 캐시 무효화 정책 구현

3. **연결 풀 튜닝**
   - R2DBC 연결 풀 크기 최적화
   - Redis 연결 풀 설정
   - 연결 타임아웃 조정

4. **비동기 처리 개선**
   - 병렬 처리 활용
   - 배치 처리 도입
   - 불필요한 순차 처리 제거 