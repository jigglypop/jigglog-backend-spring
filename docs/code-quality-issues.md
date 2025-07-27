# 코드 품질 문제점 분석

## 1. 예외 처리 일관성 부족 🔴

### 문제점
여러 서비스에서 서로 다른 예외 처리 방식 사용:

```kotlin
// AuthService.kt
if (it == null) {
    throw error("유저가 없습니다") // error() 함수 사용
} else {
    Mono.just(it)
}

// ValidationService.kt  
if (it) {
    throw Exception("이미 같은 이름의 유저가 있습니다") // Exception 직접 throw
} else {
    userForm.toMono()
}

// PasswordService.kt
} else {
    Mono.error(Exception("비밀번호가 일치하지 않습니다.")) // Mono.error 사용
}
```

### 해결방안
```kotlin
// 커스텀 예외 클래스 정의
sealed class JigglogException(message: String) : RuntimeException(message) {
    class UserNotFoundException(message: String = "사용자를 찾을 수 없습니다") : JigglogException(message)
    class InvalidPasswordException(message: String = "비밀번호가 일치하지 않습니다") : JigglogException(message)
    class DuplicateUsernameException(message: String = "이미 존재하는 사용자명입니다") : JigglogException(message)
}

// 통일된 예외 처리
return userRepository.findById(userId)
    .switchIfEmpty(Mono.error(UserNotFoundException()))
```

## 2. 코드 중복 🟡

### 문제점
비슷한 로직이 여러 곳에 반복됨:

```kotlin
// AuthHandler.kt - 여러 메서드에서 반복되는 패턴
.flatMap {
    ok().header("token", "Bearer " + it.t1).body(Mono.just(it.t2))
}.onErrorResume(Exception::class.java) {
    badRequest().body(Mono.just(it))
}
```

### 해결방안
```kotlin
// 공통 응답 처리 함수 추출
class ResponseHelper {
    companion object {
        fun <T> successWithToken(token: String, body: T): Mono<ServerResponse> {
            return ok().header("token", "Bearer $token").body(Mono.just(body))
        }
        
        fun errorResponse(error: Throwable): Mono<ServerResponse> {
            logger.error("Request failed", error)
            return badRequest().body(Mono.just(mapOf("error" to error.message)))
        }
    }
}
```

## 3. 네이밍 컨벤션 문제 🟡

### 문제점
일관성 없는 변수명과 함수명:

```kotlin
// 오타: deltePost -> deletePost
fun deltePost(postId: Int): Mono<Boolean>

// 혼재된 언어: 영어/한글 주석 혼용
// 유저 생성
fun createUser(userForm: UserFormDTO): Mono<User>

// 약어 사용 불일치
fun getUserById(userId: Int): Mono<User>
fun getUser(user: User): Mono<User> // 의미 불분명
```

### 해결방안
```kotlin
// 일관된 네이밍 규칙 적용
fun deletePost(postId: Int): Mono<Boolean>
fun findUserById(userId: Int): Mono<User>
fun validateUser(user: User): Mono<User>

// 의미 명확한 함수명 사용
fun getUserByIdOrThrow(userId: Int): Mono<User>
fun findUserByUsernameWithCache(username: String): Mono<User>
```

## 4. 매직 넘버 사용 🟡

### 문제점
```kotlin
// PostRepository.kt
"LIMIT :limit OFFSET :offset;" // 기본값 8이 여러 곳에 하드코딩

// SecurityService.kt
private val JWT_EXPIRATION_MS = 604800000 // 의미 불분명한 숫자

// UserCacheRepository.kt
val DAYS_TO_LIVE = 1L // 상수명과 실제 사용처 불일치
```

### 해결방안
```kotlin
object Constants {
    const val DEFAULT_PAGE_SIZE = 8
    const val JWT_EXPIRATION_DAYS = 7
    const val CACHE_EXPIRATION_DAYS = 1L
    
    val JWT_EXPIRATION_MS = TimeUnit.DAYS.toMillis(JWT_EXPIRATION_DAYS.toLong())
}
```

## 5. 불필요한 어노테이션 사용 🟡

### 문제점
```kotlin
// PasswordService.kt
@Controller // 서비스 계층인데 @Controller 사용
class PasswordService

// AuthService.kt  
@Controller // 서비스 계층인데 @Controller 사용
class AuthService
```

### 해결방안
```kotlin
@Service // 비즈니스 로직은 @Service 사용
class PasswordService

@Service
class AuthService
```

## 6. 로깅 개선 필요 🟡

### 문제점
```kotlin
// 로깅 레벨과 내용이 부적절
logger.info("패스워드 해싱") // 보안상 민감한 정보

// 로깅이 없는 중요한 로직들
fun deletePost(postId: Int): Mono<Boolean> {
    return postRepository.deleteById(postId).thenReturn(true)
    // 삭제 로깅 없음
}
```

### 해결방안
```kotlin
// 적절한 로깅 레벨과 내용
logger.debug("Password encoding initiated for user authentication")
logger.info("Post deleted successfully: postId={}", postId)
logger.warn("Failed login attempt for username: {}", username)
logger.error("Database connection failed", exception)
```

## 7. Null 안전성 개선 🟡

### 문제점
```kotlin
// User.kt - nullable 필드에 기본값 제공으로 혼란 야기
val username: String? = "", // null과 빈 문자열이 혼재
val email: String? = "",
```

### 해결방안
```kotlin
// 명확한 null 처리
data class User(
    val id: Int = 0,
    val username: String, // 필수 필드는 non-null
    val email: String?, // 선택 필드는 null 허용, 기본값 없음
    var hashedPassword: String,
    val imageUrl: String? = null,
    val githubUrl: String? = null,
    val summary: String? = null
)
```

## 8. 함수 복잡도 🟡

### 문제점
```kotlin
// PostService.createPost - 너무 많은 책임을 가진 함수
fun createPost(user: User, postForm: PostFormDTO, category: Category, tags: List<Tag>): Mono<PostDTO> {
    // 포스트 저장 + 태그 연결 + 결과 조회를 모두 수행
}
```

### 해결방안
```kotlin
// 단일 책임으로 분리
fun createPost(postForm: PostFormDTO, userId: Int, categoryId: Int): Mono<Post>
fun linkPostToTags(postId: Int, tagIds: List<Int>): Mono<List<PostToTag>>
fun getPostWithDetails(postId: Int): Mono<PostDTO>

// 조합하여 사용
fun createPostWithTags(user: User, postForm: PostFormDTO, category: Category, tags: List<Tag>): Mono<PostDTO> {
    return createPost(postForm, user.id, category.id)
        .flatMap { post -> linkPostToTags(post.id, tags.map { it.id }) }
        .flatMap { getPostWithDetails(it.first().postId) }
}
```

## 권장 개선사항

1. **예외 처리 표준화**: 커스텀 예외 클래스 도입
2. **코드 중복 제거**: 공통 로직 유틸리티 클래스로 추출  
3. **네이밍 규칙 통일**: 프로젝트 전체 네이밍 컨벤션 수립
4. **상수 관리**: 매직 넘버를 상수 클래스로 관리
5. **로깅 전략 수립**: 로그 레벨과 내용 가이드라인 작성 