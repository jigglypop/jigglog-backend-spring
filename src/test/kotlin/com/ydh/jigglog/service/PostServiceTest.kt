package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.PostFormDTO
import com.ydh.jigglog.domain.dto.UpdateFormDTO
import com.ydh.jigglog.domain.entity.*
import com.ydh.jigglog.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PostServiceTest {

    @Mock
    private lateinit var postRepository: PostRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var postToTagRepository: PostToTagRepository

    @Mock
    private lateinit var resumeCacheRepository: ResumeCacheRepository

    private lateinit var postService: PostService

    private lateinit var testUser: User
    private lateinit var testCategory: Category
    private lateinit var testTags: List<Tag>
    private lateinit var testPost: Post
    private lateinit var testPostForm: PostFormDTO

    @BeforeEach
    fun setUp() {
        postService = PostService(
            postRepository,
            userRepository,
            categoryRepository,
            postToTagRepository,
            resumeCacheRepository
        )

        testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            hashedPassword = "hashedPassword",
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

        testPost = Post().apply {
            id = 1
            title = "테스트 게시글"
            summary = "테스트 요약"
            content = "테스트 내용"
            images = "test.jpg"
            userId = 1
            categoryId = 1
            viewcount = 0
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }

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
    fun `게시글을 성공적으로 생성할 수 있다`() {
        val savedPost = Post().apply {
            id = 1
            title = "테스트 게시글"
            summary = "테스트 요약"
            content = "테스트 내용"
            images = "test.jpg"
            userId = 1
            categoryId = 1
            viewcount = 1
        }
        val postToTags = testTags.map { tag ->
            PostToTag().apply {
                postId = 1
                tagId = tag.id
            }
        }

        whenever(postRepository.save(any<Post>())).thenReturn(Mono.just(savedPost))
        whenever(postToTagRepository.saveAll(any<List<PostToTag>>())).thenReturn(Flux.fromIterable(postToTags))
        whenever(postRepository.findById(1)).thenReturn(Mono.just(savedPost))
        whenever(postRepository.findTagsByPostId(1)).thenReturn(Flux.fromIterable(testTags))
        whenever(userRepository.findById(1)).thenReturn(Mono.just(testUser))
        whenever(categoryRepository.findById(1)).thenReturn(Mono.just(testCategory))
        whenever(postRepository.existsById(1)).thenReturn(Mono.just(true))

        val result = postService.createPost(testUser, testPostForm, testCategory, testTags)

        StepVerifier.create(result)
            .expectNextMatches { postDTO ->
                postDTO.title == "테스트 게시글" &&
                postDTO.summary == "테스트 요약" &&
                postDTO.content == "테스트 내용" &&
                postDTO.user?.username == "testuser" &&
                postDTO.category?.title == "테스트 카테고리" &&
                postDTO.tags?.size == 2
            }
            .verifyComplete()

        verify(postRepository, atLeastOnce()).save(any<Post>())
        verify(postToTagRepository).saveAll(any<List<PostToTag>>())
    }

    @Test
    fun `게시글 ID로 단일 게시글을 조회할 수 있다`() {
        whenever(postRepository.findById(1)).thenReturn(Mono.just(testPost))

        val result = postService.getOnlyPost(1)

        StepVerifier.create(result)
            .expectNextMatches { post ->
                post.id == 1 &&
                post.title == "테스트 게시글"
            }
            .verifyComplete()

        verify(postRepository).findById(1)
    }

    @Test
    fun `게시글 상세 정보를 조회할 수 있다`() {
        val updatedPost = Post().apply {
            id = 1
            title = "테스트 게시글"
            summary = "테스트 요약"
            content = "테스트 내용"
            images = "test.jpg"
            userId = 1
            categoryId = 1
            viewcount = 1
        }

        whenever(postRepository.existsById(1)).thenReturn(Mono.just(true))
        whenever(postRepository.findById(1)).thenReturn(Mono.just(testPost))
        whenever(postRepository.save(any<Post>())).thenReturn(Mono.just(updatedPost))
        whenever(postRepository.findTagsByPostId(1)).thenReturn(Flux.fromIterable(testTags))
        whenever(userRepository.findById(1)).thenReturn(Mono.just(testUser))
        whenever(categoryRepository.findById(1)).thenReturn(Mono.just(testCategory))

        val result = postService.getPost(1)

        StepVerifier.create(result)
            .expectNextMatches { postDTO ->
                postDTO?.title == "테스트 게시글" &&
                postDTO?.viewcount == 1 &&
                postDTO?.user?.username == "testuser" &&
                postDTO?.user?.hashedPassword == "" &&
                postDTO?.category?.title == "테스트 카테고리" &&
                postDTO?.tags?.size == 2
            }
            .verifyComplete()

        verify(postRepository).existsById(1)
        verify(postRepository).save(any<Post>())
        verify(userRepository).findById(1)
        verify(categoryRepository).findById(1)
    }

    @Test
    fun `존재하지 않는 게시글 조회시 예외가 발생한다`() {
        whenever(postRepository.existsById(999)).thenReturn(Mono.just(false))

        val result = postService.getPost(999)

        StepVerifier.create(result)
            .expectErrorMatches { error ->
                error is IllegalStateException && error.message == "포스트가 없습니다"
            }
            .verify()

        verify(postRepository).existsById(999)
        verify(postRepository, never()).findById(999)
    }

    @Test
    fun `게시글 경로 목록을 조회할 수 있다`() {
        val posts = listOf(
            testPost,
            Post().apply {
                id = 2
                title = "두 번째 게시글"
                summary = "테스트 요약"
                content = "테스트 내용"
                images = "test.jpg"
                userId = 1
                categoryId = 1
            }
        )

        whenever(postRepository.findAll()).thenReturn(Flux.fromIterable(posts))

        val result = postService.getPostPath()

        StepVerifier.create(result)
            .expectNextMatches { pathList ->
                pathList.size == 2 &&
                pathList[0].title == "테스트 게시글" &&
                pathList[1].title == "두 번째 게시글"
            }
            .verifyComplete()

        verify(postRepository).findAll()
    }

    @Test
    fun `게시글을 업데이트할 수 있다`() {
        val updateForm = UpdateFormDTO().apply {
            title = "수정된 제목"
            summary = "수정된 요약"
            content = "수정된 내용"
            images = "updated.jpg"
        }
        val updatedPost = Post().apply {
            id = 1
            title = "수정된 제목"
            summary = "수정된 요약"
            content = "수정된 내용"
            images = "updated.jpg"
            userId = 1
            categoryId = 1
        }

        whenever(postRepository.save(any<Post>())).thenReturn(Mono.just(updatedPost))
        whenever(postRepository.existsById(1)).thenReturn(Mono.just(true))
        whenever(postRepository.findById(1)).thenReturn(Mono.just(updatedPost))
        whenever(postRepository.findTagsByPostId(1)).thenReturn(Flux.fromIterable(testTags))
        whenever(userRepository.findById(1)).thenReturn(Mono.just(testUser))
        whenever(categoryRepository.findById(1)).thenReturn(Mono.just(testCategory))

        val result = postService.updatePost(testPost, updateForm)

        StepVerifier.create(result)
            .expectNextMatches { postDTO ->
                postDTO.title == "수정된 제목" &&
                postDTO.summary == "수정된 요약" &&
                postDTO.content == "수정된 내용" &&
                postDTO.images == "updated.jpg"
            }
            .verifyComplete()

        verify(postRepository, times(2)).save(any<Post>())
    }

    @Test
    fun `게시글을 삭제할 수 있다`() {
        whenever(postRepository.deleteById(1)).thenReturn(Mono.empty())

        val result = postService.deltePost(1)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        verify(postRepository).deleteById(1)
    }

    @Test
    fun `이력서를 조회할 수 있다`() {
        val resumePost = Post().apply {
            id = 1
            title = "이력서"
            summary = "이력서 요약"
            content = "이력서 내용"
        }
        whenever(resumeCacheRepository.findResumeAndCaching()).thenReturn(Mono.just(resumePost))

        val result = postService.getResume()

        StepVerifier.create(result)
            .expectNextMatches { post ->
                post.title == "이력서"
            }
            .verifyComplete()

        verify(resumeCacheRepository).findResumeAndCaching()
    }
} 