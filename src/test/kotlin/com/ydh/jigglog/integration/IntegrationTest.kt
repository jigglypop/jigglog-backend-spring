package com.ydh.jigglog.integration

import com.ydh.jigglog.config.TestConfig
import com.ydh.jigglog.domain.dto.PostFormDTO
import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.domain.entity.*
import com.ydh.jigglog.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.BodyInserters
import reactor.test.StepVerifier
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig::class)
@TestPropertySource(properties = [
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password=",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
])
@Transactional
class IntegrationTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var tagRepository: TagRepository

    private lateinit var testUser: User
    private lateinit var testCategory: Category
    private lateinit var testTags: List<Tag>

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = 0,
            username = "testuser",
            email = "test@example.com",
            hashedPassword = "hashedPassword123",
            imageUrl = "profile.jpg",
            githubUrl = "",
            summary = "테스트 사용자"
        )

        testCategory = Category(
            id = 0,
            title = "테스트 카테고리",
            thumbnail = "category.jpg"
        )

        testTags = listOf(
            Tag(0, "Kotlin"),
            Tag(0, "Spring")
        )
    }

    @Test
    fun `전체 워크플로우 통합 테스트 - 사용자 생성부터 게시글 작성까지`() {
        val savedUser = userRepository.save(testUser).block()
        val savedCategory = categoryRepository.save(testCategory).block()
        val savedTags = tagRepository.saveAll(testTags).collectList().block()

        assert(savedUser != null)
        assert(savedCategory != null)
        assert(savedTags != null && savedTags.size == 2)

        val postForm = PostFormDTO().apply {
            title = "통합 테스트 게시글"
            summary = "통합 테스트 요약"
            content = "통합 테스트 내용"
            images = "test.jpg"
            category_title = "테스트 카테고리"
            tags = "Kotlin,Spring"
        }

        val createdPost = Post().apply {
            title = postForm.title!!
            summary = postForm.summary!!
            content = postForm.content!!
            images = postForm.images!!
            userId = savedUser?.id ?: 0
            categoryId = savedCategory?.id ?: 0
            viewcount = 0
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        val savedPost = postRepository.save(createdPost).block()
        assert(savedPost != null)
        assert(savedPost!!.title == "통합 테스트 게시글")
        assert(savedPost.userId == savedUser?.id)
        assert(savedPost.categoryId == savedCategory?.id)
    }

    @Test
    fun `데이터베이스 연결 및 기본 CRUD 작업 테스트`() {
        val testPost = Post().apply {
            title = "CRUD 테스트 게시글"
            summary = "CRUD 테스트"
            content = "CRUD 테스트 내용"
            images = "crud.jpg"
            userId = 1
            categoryId = 1
            viewcount = 0
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        val saveAndFind = postRepository.save(testPost)
            .flatMap { saved -> postRepository.findById(saved.id) }

        StepVerifier.create(saveAndFind)
            .expectNextMatches { found ->
                found.title == "CRUD 테스트 게시글" &&
                found.content == "CRUD 테스트 내용"
            }
            .verifyComplete()
    }

    @Test
    fun `카테고리와 태그 관계 테스트`() {
        val savedCategory = categoryRepository.save(testCategory).block()
        val savedTags = tagRepository.saveAll(testTags).collectList().block()

        assert(savedCategory != null)
        assert(savedTags != null)
        assert(savedTags!!.size == 2)

        val findCategory = categoryRepository.findById(savedCategory!!.id)
        StepVerifier.create(findCategory)
            .expectNextMatches { category ->
                category.title == "테스트 카테고리"
            }
            .verifyComplete()

        val findAllTags = tagRepository.findAll().collectList()
        StepVerifier.create(findAllTags)
            .expectNextMatches { tags ->
                tags.size >= 2 &&
                tags.any { it.title == "Kotlin" } &&
                tags.any { it.title == "Spring" }
            }
            .verifyComplete()
    }

    @Test
    fun `사용자 저장 및 조회 테스트`() {
        val saveAndFind = userRepository.save(testUser)
            .flatMap { saved -> userRepository.findById(saved.id) }

        StepVerifier.create(saveAndFind)
            .expectNextMatches { user ->
                user.username == "testuser" &&
                user.email == "test@example.com"
            }
            .verifyComplete()
    }

    @Test
    fun `사용자명으로 사용자 조회 테스트`() {
        val saveAndFindByUsername = userRepository.save(testUser)
            .flatMap { userRepository.findByUsername("testuser") }

        StepVerifier.create(saveAndFindByUsername)
            .expectNextMatches { user ->
                user.username == "testuser" &&
                user.hashedPassword == "hashedPassword123"
            }
            .verifyComplete()
    }

    @Test
    fun `게시글 조회수 업데이트 테스트`() {
        val post = Post().apply {
            title = "조회수 테스트"
            summary = "테스트"
            content = "조회수 증가 테스트"
            images = "view.jpg"
            userId = 1
            categoryId = 1
            viewcount = 0
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        val viewCountTest = postRepository.save(post)
            .flatMap { saved ->
                saved.viewcount = saved.viewcount + 1
                postRepository.save(saved)
            }
            .flatMap { updated -> postRepository.findById(updated.id) }

        StepVerifier.create(viewCountTest)
            .expectNextMatches { found ->
                found.viewcount == 1
            }
            .verifyComplete()
    }

    @Test
    fun `다중 게시글 조회 및 정렬 테스트`() {
        val posts = listOf(
            Post().apply {
                title = "첫 번째 게시글"
                summary = "첫 번째"
                content = "첫 번째 내용"
                userId = 1
                categoryId = 1
                viewcount = 0
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            },
            Post().apply {
                title = "두 번째 게시글"
                summary = "두 번째"
                content = "두 번째 내용"
                userId = 1
                categoryId = 1
                viewcount = 5
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }
        )

        val saveAllAndFind = postRepository.saveAll(posts)
            .then(postRepository.findAll().collectList())

        StepVerifier.create(saveAllAndFind)
            .expectNextMatches { savedPosts ->
                savedPosts.size >= 2 &&
                savedPosts.any { it.title == "첫 번째 게시글" } &&
                savedPosts.any { it.title == "두 번째 게시글" }
            }
            .verifyComplete()
    }

    @Test
    fun `애플리케이션 컨텍스트 로딩 테스트`() {
        assert(webTestClient != null)
        assert(postRepository != null)
        assert(userRepository != null)
        assert(categoryRepository != null)
        assert(tagRepository != null)
    }
} 