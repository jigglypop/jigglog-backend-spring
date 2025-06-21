package com.ydh.jigglog.handler

import com.ydh.jigglog.config.TestConfig
import com.ydh.jigglog.router.PostRouter
import com.ydh.jigglog.domain.dto.PostFormDTO
import com.ydh.jigglog.domain.dto.UpdateFormDTO
import com.ydh.jigglog.domain.entity.*
import com.ydh.jigglog.service.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@WebFluxTest
@Import(PostHandler::class, PostRouter::class, TestConfig::class)
class PostHandlerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var postService: PostService

    @MockBean
    private lateinit var securityService: SecurityService

    @MockBean
    private lateinit var validationService: ValidationService

    @MockBean
    private lateinit var categoryService: CategoryService

    @MockBean
    private lateinit var tagService: TagService

    @MockBean
    private lateinit var postToTagService: PostToTagService

    private lateinit var testUser: User
    private lateinit var testCategory: Category
    private lateinit var testTags: List<Tag>
    private lateinit var testPostForm: PostFormDTO

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            hashedPassword = "",
            imageUrl = "profile.jpg",
            githubUrl = "",
            summary = "테스트 사용자"
        )
        testCategory = Category(
            id = 1,
            title = "테스트 카테고리",
            thumbnail = ""
        )
        testTags = listOf(
            Tag(1, "Kotlin"),
            Tag(2, "Spring")
        )
        testPostForm = PostFormDTO().apply {
            title = "테스트 게시글"
            summary = "테스트 요약"
            content = "테스트 내용"
            images = "test.jpg"
            category_title = "테스트 카테고리"
            tags = "Kotlin,Spring"
        }
    }

    @Test
    fun `게시글 생성 요청이 성공적으로 처리된다`() {
        val expectedPostDTO = createMockPostDTO()

        whenever(validationService.checkValidForm<PostFormDTO>(any(), any())).thenReturn(Mono.just(testPostForm))
        whenever(securityService.getLoggedInUser(any())).thenReturn(Mono.just(testUser))
        whenever(securityService.isOwner(testUser)).thenReturn(Mono.just(true))
        whenever(tagService.createTagParseAndMakeAll(any())).thenReturn(Mono.just(testTags.toMutableList()))
        whenever(categoryService.createCategoryIfNot(any())).thenReturn(Mono.just(testCategory))
        whenever(postService.createPost(any(), any(), any(), any())).thenReturn(Mono.just(expectedPostDTO))
        whenever(categoryService.resetCategoryCash()).thenReturn(Mono.empty())

        webTestClient.post()
            .uri("/api/post")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(testPostForm))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.title").isEqualTo("테스트 게시글")
            .jsonPath("$.summary").isEqualTo("테스트 요약")
            .jsonPath("$.content").isEqualTo("테스트 내용")
            .jsonPath("$.user.username").isEqualTo("testuser")
            .jsonPath("$.category.title").isEqualTo("테스트 카테고리")
            .jsonPath("$.tags").isArray
            .jsonPath("$.tags.length()").isEqualTo(2)
    }

    @Test
    fun `잘못된 요청 데이터로 게시글 생성시 400 에러를 반환한다`() {
        val invalidPostForm = PostFormDTO().apply {
            title = ""
            summary = ""
            content = ""
            images = ""
            category_title = ""
            tags = ""
        }

        whenever(validationService.checkValidForm<PostFormDTO>(any(), any()))
            .thenThrow(IllegalArgumentException("포스트 제목이 비어있습니다"))

        webTestClient.post()
            .uri("/api/post")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidPostForm))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("포스트 제목이 비어있습니다")
    }

    @Test
    fun `게시글 단일 조회가 성공적으로 처리된다`() {
        val expectedPostDTO = createMockPostDTO()
        whenever(postService.getPost(1)).thenReturn(Mono.just(expectedPostDTO))
        webTestClient.get()
            .uri("/api/post/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.title").isEqualTo("테스트 게시글")
            .jsonPath("$.summary").isEqualTo("테스트 요약")
            .jsonPath("$.content").isEqualTo("테스트 내용")
            .jsonPath("$.viewcount").isEqualTo(1)
            .jsonPath("$.user.username").isEqualTo("testuser")
            .jsonPath("$.category.title").isEqualTo("테스트 카테고리")
    }

    @Test
    fun `존재하지 않는 게시글 조회시 400 에러를 반환한다`() {
        whenever(postService.getPost(999))
            .thenReturn(Mono.error(IllegalStateException("포스트가 없습니다")))

        webTestClient.get()
            .uri("/api/post/999")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("포스트가 없습니다")
    }

    @Test
    fun `게시글 경로 목록 조회가 성공적으로 처리된다`() {
        val postPaths = listOf(
            com.ydh.jigglog.domain.dto.PostPathDTO().apply {
                id = 1
                title = "첫 번째 게시글"
            },
            com.ydh.jigglog.domain.dto.PostPathDTO().apply {
                id = 2
                title = "두 번째 게시글"
            }
        )

        whenever(postService.getPostPath()).thenReturn(Mono.just(postPaths))

        webTestClient.get()
            .uri("/api/post/path")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isEqualTo(2)
            .jsonPath("$[0].id").isEqualTo(1)
            .jsonPath("$[0].title").isEqualTo("첫 번째 게시글")
            .jsonPath("$[1].id").isEqualTo(2)
            .jsonPath("$[1].title").isEqualTo("두 번째 게시글")
    }

    @Test
    fun `게시글 업데이트가 성공적으로 처리된다`() {
        val updateForm = UpdateFormDTO().apply {
            title = "수정된 제목"
            summary = "수정된 요약"
            content = "수정된 내용"
            images = "updated.jpg"
        }
        val updatedPostDTO = com.ydh.jigglog.domain.dto.PostDTO().apply {
            id = 1
            title = "수정된 제목"
            summary = "수정된 요약"
            content = "수정된 내용"
            images = "updated.jpg"
            viewcount = 1
            site = null
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
            user = testUser
            category = testCategory
            tags = testTags.toMutableList()
        }
        val originalPost = Post().apply {
            id = 1
            title = "원본 제목"
            summary = "원본 요약"
            content = "원본 내용"
            images = "original.jpg"
            userId = 1
            categoryId = 1
            viewcount = 1
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

        whenever(securityService.getLoggedInUser(any())).thenReturn(Mono.just(testUser))
        whenever(postService.getOnlyPost(1)).thenReturn(Mono.just(originalPost))
        whenever(securityService.isOwner(testUser)).thenReturn(Mono.just(true))
        whenever(postService.updatePost(originalPost, updateForm)).thenReturn(Mono.just(updatedPostDTO))

        webTestClient.patch()
            .uri("/api/post/1")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(updateForm))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.title").isEqualTo("수정된 제목")
            .jsonPath("$.summary").isEqualTo("수정된 요약")
            .jsonPath("$.content").isEqualTo("수정된 내용")
            .jsonPath("$.images").isEqualTo("updated.jpg")
    }

    @Test
    fun `권한이 없는 사용자의 게시글 업데이트 요청시 400 에러를 반환한다`() {
        val updateForm = UpdateFormDTO().apply {
            title = "수정된 제목"
            summary = null
            content = null
            images = null
        }

        whenever(securityService.getLoggedInUser(any())).thenReturn(Mono.just(testUser))
        whenever(securityService.isOwner(testUser))
            .thenReturn(Mono.error(SecurityException("권한이 없습니다")))

        webTestClient.patch()
            .uri("/api/post/1")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(updateForm))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("권한이 없습니다")
    }

    @Test
    fun `게시글 삭제가 성공적으로 처리된다`() {
        whenever(securityService.getLoggedInUser(any())).thenReturn(Mono.just(testUser))
        whenever(securityService.isOwner(testUser)).thenReturn(Mono.just(true))
        whenever(postService.deltePost(1)).thenReturn(Mono.just(true))
        whenever(categoryService.resetCategoryCash()).thenReturn(Mono.empty())

        webTestClient.delete()
            .uri("/api/post/1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("포스트 삭제가 완료되었습니다.")
    }

    @Test
    fun `권한이 없는 사용자의 게시글 삭제 요청시 400 에러를 반환한다`() {
        whenever(securityService.getLoggedInUser(any())).thenReturn(Mono.just(testUser))
        whenever(securityService.isOwner(testUser))
            .thenReturn(Mono.error(SecurityException("권한이 없습니다")))

        webTestClient.delete()
            .uri("/api/post/1")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("권한이 없습니다")
    }

    private fun createMockPostDTO() = com.ydh.jigglog.domain.dto.PostDTO().apply {
        id = 1
        title = "테스트 게시글"
        summary = "테스트 요약"
        content = "테스트 내용"
        images = "test.jpg"
        viewcount = 1
        site = null
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
        user = testUser
        category = testCategory
        tags = testTags
    }
} 