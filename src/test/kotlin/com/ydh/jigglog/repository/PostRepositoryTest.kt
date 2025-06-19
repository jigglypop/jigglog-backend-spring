package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.Post
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password="
])
class PostRepositoryTest {

    @Autowired
    private lateinit var postRepository: PostRepository

    private lateinit var testPost: Post

    @BeforeEach
    fun setUp() {
        testPost = Post().apply {
            title = "테스트 게시글"
            summary = "테스트 요약"
            content = "테스트 내용입니다."
            images = "test-image.jpg"
            userId = 1
            categoryId = 1
            viewcount = 0
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }
    }

    @Test
    fun `게시글을 저장하고 조회할 수 있다`() {
        val savedPost = postRepository.save(testPost)

        StepVerifier.create(savedPost)
            .expectNextMatches { post ->
                post.id != 0 &&
                post.title == "테스트 게시글" &&
                post.summary == "테스트 요약" &&
                post.content == "테스트 내용입니다." &&
                post.userId == 1 &&
                post.categoryId == 1
            }
            .verifyComplete()
    }

    @Test
    fun `ID로 게시글을 조회할 수 있다`() {
        val savedAndFoundPost = postRepository.save(testPost)
            .flatMap { saved -> postRepository.findById(saved.id) }

        StepVerifier.create(savedAndFoundPost)
            .expectNextMatches { post ->
                post.title == "테스트 게시글" &&
                post.content == "테스트 내용입니다."
            }
            .verifyComplete()
    }

    @Test
    fun `존재하지 않는 ID로 조회시 빈 결과를 반환한다`() {
        val notFoundPost = postRepository.findById(999)

        StepVerifier.create(notFoundPost)
            .verifyComplete()
    }

    @Test
    fun `게시글 존재 여부를 확인할 수 있다`() {
        val existsCheck = postRepository.save(testPost)
            .flatMap { saved -> postRepository.existsById(saved.id) }

        StepVerifier.create(existsCheck)
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `존재하지 않는 게시글의 존재 여부는 false를 반환한다`() {
        val notExistsCheck = postRepository.existsById(999)

        StepVerifier.create(notExistsCheck)
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `모든 게시글을 조회할 수 있다`() {
        val testPost2 = Post().apply {
            title = "두 번째 게시글"
            summary = "테스트 요약"
            content = "테스트 내용입니다."
            images = "test-image.jpg"
            userId = 1
            categoryId = 1
            viewcount = 0
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }
        
        val allPosts = postRepository.save(testPost)
            .then(postRepository.save(testPost2))
            .then(postRepository.findAll().collectList())

        StepVerifier.create(allPosts)
            .expectNextMatches { posts ->
                posts.size >= 2 &&
                posts.any { it.title == "테스트 게시글" } &&
                posts.any { it.title == "두 번째 게시글" }
            }
            .verifyComplete()
    }

    @Test
    fun `게시글을 삭제할 수 있다`() {
        val deleteTest = postRepository.save(testPost)
            .flatMap { saved ->
                postRepository.deleteById(saved.id)
                    .then(postRepository.existsById(saved.id))
            }

        StepVerifier.create(deleteTest)
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `게시글 조회수를 업데이트할 수 있다`() {
        val viewCountTest = postRepository.save(testPost)
            .flatMap { saved ->
                saved.viewcount = saved.viewcount + 1
                postRepository.save(saved)
            }

        StepVerifier.create(viewCountTest)
            .expectNextMatches { post ->
                post.viewcount == 1
            }
            .verifyComplete()
    }
} 