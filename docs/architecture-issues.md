# 아키텍처 문제점 분석

## 1. 계층 분리 미흡 🟡

### 문제점
서비스 계층에서 데이터 액세스 로직과 비즈니스 로직이 혼재됨:

```kotlin
// PostService.kt - 서비스에서 직접 복잡한 데이터 조합 로직 수행
fun getPost(postId: Int): Mono<PostDTO?> {
    return Mono.just(postId)
        .flatMap { postRepository.existsById(it) }
        .flatMap { isExist ->
            if (!isExist) {
                throw error("포스트가 없습니다")
            } else {
                postRepository.findById(postId)
                    .flatMap { post ->
                        postRepository.save(post.apply { viewcount++ }).toMono()
                    }.flatMap { post ->
                        Mono.zip(
                            post.toMono(),
                            postRepository.findTagsByPostId(postId).collectList().toMono(),
                            userRepository.findById(post.userId!!).flatMap { ... },
                            categoryRepository.findById(post.categoryId!!).toMono(),
                        )
                    }
            }
        }
}
```

### 해결방안
```kotlin
// 도메인 서비스 계층 도입
class PostDomainService {
    fun incrementViewCount(post: Post): Post = post.apply { viewcount++ }
    fun validatePostExists(exists: Boolean): Unit = 
        if (!exists) throw PostNotFoundException()
}

// 리포지토리에서 조합 로직 처리
interface PostRepository {
    fun findPostWithDetails(postId: Int): Mono<PostWithDetails>
}

// 서비스는 비즈니스 로직에 집중
class PostService {
    fun getPost(postId: Int): Mono<PostDTO> {
        return postRepository.findPostWithDetails(postId)
            .doOnNext { postDomainService.incrementViewCount(it.post) }
            .map { it.toDTO() }
    }
}
```

## 2. 의존성 주입 방식 혼재 🟡

### 문제점
생성자 주입, 필드 주입, @Autowired가 혼재되어 사용됨:

```kotlin
// AuthHandler.kt - 생성자와 @Autowired 혼재
class AuthHandler(
    @param:Value("\${spring.datasource.secretuser}") private val secretuser: String,
    @param:Value("\${spring.datasource.secretpassword}") private val secretpassword: String,
    @Autowired val securityService: SecurityService, // @Autowired 사용
    @Autowired val passwordService: PasswordService,
    // ...
)

// PostService.kt - 생성자에서 @Autowired 사용
class PostService (
    @Autowired private val postRepository: PostRepository,
    @Autowired private val userRepository: UserRepository,
    // ...
)
```

### 해결방안
```kotlin
// 일관된 생성자 주입 사용
class AuthHandler(
    @Value("\${app.secret.user}") private val secretUser: String,
    @Value("\${app.secret.password}") private val secretPassword: String,
    private val securityService: SecurityService, // @Autowired 제거
    private val passwordService: PasswordService,
    private val validationService: ValidationService,
    private val authService: AuthService,
    private val redisTemplate: ReactiveRedisTemplate<String, User>
)

class PostService(
    private val postRepository: PostRepository, // @Autowired 제거
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val postToTagRepository: PostToTagRepository,
    private val resumeCacheRepository: ResumeCacheRepository
)
```

## 3. 캐싱 전략 비일관성 🟡

### 문제점
캐시 사용이 일부 기능에만 적용되고 전략이 일관되지 않음:

```kotlin
// UserCacheRepository.kt - 수동 캐시 관리
fun findByNameWithCaching(username: String): Mono<User> {
    return reactiveRedisTemplate.opsForValue().get(username)
        .switchIfEmpty(setUserMono)
}

// AuthService.kt - 주석 처리된 캐시 로직
fun getUserByUsername(username: String): Mono<User> {
//        return userRepository.findByUsername(username).cache().flatMap {
//            ...
//        }
    return userCacheRepository.findByNameWithCaching(username)
}
```

### 해결방안
```kotlin
// 일관된 캐시 추상화 계층
interface CacheRepository<T, K> {
    fun findWithCache(key: K): Mono<T>
    fun saveToCache(key: K, value: T): Mono<Void>
    fun evictFromCache(key: K): Mono<Void>
    fun evictAllFromCache(): Mono<Void>
}

// 캐시 정책 설정 클래스
@Configuration
class CacheConfig {
    @Bean
    fun userCachePolicy(): CachePolicy = CachePolicy(
        ttl = Duration.ofHours(1),
        evictionPolicy = EvictionPolicy.LRU,
        maxSize = 1000
    )
}
```

## 4. 라우터와 핸들러 구조 개선 필요 🟡

### 문제점
라우터에서 모든 경로를 하나의 메서드에 정의:

```kotlin
// AuthRouter.kt - 평면적인 라우팅 구조
@Bean
fun authRouterFunction() = router {
    accept(MediaType.APPLICATION_JSON).nest {
        "/api/auth".nest {
            GET("/test", handler::test)
            POST("/register", handler::register)
            POST("/login", handler::login)
            POST("/comment", handler::comment)
            GET("/check", handler::check)
        }
    }
}
```

### 해결방안
```kotlin
// 기능별 라우터 분리
@Configuration
class RouterConfig {
    
    @Bean
    fun authRoutes(authHandler: AuthHandler) = router {
        "/api/auth".nest {
            POST("/register", authHandler::register)
            POST("/login", authHandler::login)
            GET("/check", authHandler::check)
        }
    }
    
    @Bean  
    fun commentRoutes(commentHandler: CommentHandler) = router {
        "/api/comments".nest {
            POST("/", commentHandler::createComment)
            GET("/{postId}", commentHandler::getCommentsByPost)
        }
    }
    
    @Bean
    fun mainRouter(authRoutes: RouterFunction<ServerResponse>, 
                   commentRoutes: RouterFunction<ServerResponse>) = 
        authRoutes.and(commentRoutes)
}
```

## 5. 도메인 모델 설계 문제 🟡

### 문제점
엔티티 클래스에서 비즈니스 로직 부재:

```kotlin
// User.kt - 단순 데이터 클래스
@Table(name = "user")
class User(
    @Id var id: Int = 0,
    @Column("username") val username: String? = "",
    // ... 기본값으로 빈 문자열 사용
)
```

### 해결방안
```kotlin
// 도메인 로직이 포함된 엔티티
@Table(name = "user")
data class User(
    @Id val id: UserId,
    @Column("username") val username: Username,
    @Column("email") val email: Email?,
    @Column("hashedPassword") private var hashedPassword: HashedPassword,
    @Column("profile") val profile: UserProfile
) {
    fun changePassword(newPassword: String, passwordEncoder: PasswordEncoder) {
        this.hashedPassword = HashedPassword(passwordEncoder.encode(newPassword))
    }
    
    fun validatePassword(rawPassword: String, passwordEncoder: PasswordEncoder): Boolean {
        return passwordEncoder.matches(rawPassword, hashedPassword.value)
    }
    
    fun isOwner(targetUserId: UserId): Boolean = this.id == targetUserId
}

// 값 객체로 타입 안전성 확보
@JvmInline
value class UserId(val value: Int)

@JvmInline  
value class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "Username cannot be blank" }
        require(value.length in 3..20) { "Username must be between 3 and 20 characters" }
    }
}
```

## 6. 트랜잭션 관리 부재 🟡

### 문제점
연관된 데이터 변경이 트랜잭션으로 묶이지 않음:

```kotlin
// PostService.createPost - 포스트 생성과 태그 연결이 분리됨
return postRepository.save(post)
    .flatMap { post ->
        postToTagRepository.saveAll(postToTags).collectList().toMono()
    }
```

### 해결방안
```kotlin
@Service
@Transactional
class PostService {
    
    @Transactional
    fun createPostWithTags(postForm: PostFormDTO, tagIds: List<Int>): Mono<PostDTO> {
        return postRepository.save(createPostEntity(postForm))
            .flatMap { post ->
                val postToTags = tagIds.map { PostToTag(post.id, it) }
                postToTagRepository.saveAll(postToTags)
                    .then(Mono.just(post))
            }
            .flatMap { post -> getPostWithDetails(post.id) }
    }
}
```

## 7. 설정 관리 개선 🟡

### 문제점
설정이 여러 곳에 분산되어 관리가 어려움:

```kotlin
// 하드코딩된 설정값들
val JWT_EXPIRATION_MS = 604800000
val DAYS_TO_LIVE = 1L
cors.allowedOrigins = listOf("http://localhost:3000", "https://jigglog.com")
```

### 해결방안
```kotlin
// 설정 프로퍼티 클래스
@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
    val cache: CacheProperties,
    val cors: CorsProperties
)

data class JwtProperties(
    val secret: String,
    val expirationMs: Long,
    val refreshExpirationMs: Long
)

data class CacheProperties(
    val userTtlDays: Long,
    val postTtlHours: Long
)

data class CorsProperties(
    val allowedOrigins: List<String>,
    val allowedMethods: List<String>
)
```

## 권장 개선사항

1. **계층 분리**: 도메인, 애플리케이션, 인프라 계층 명확히 분리
2. **의존성 주입 통일**: 생성자 주입으로 일관성 있게 변경
3. **캐시 전략 수립**: 통합된 캐시 관리 시스템 구축
4. **도메인 모델 강화**: 비즈니스 로직을 엔티티로 이동
5. **트랜잭션 관리**: 데이터 일관성을 위한 트랜잭션 경계 설정
6. **설정 중앙화**: 외부 설정 파일과 환경변수 활용 