package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.PostFormDTO
import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class ValidationServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var validationService: ValidationService

    private lateinit var validPostForm: PostFormDTO
    private lateinit var invalidPostForm: PostFormDTO
    private lateinit var validUserForm: UserFormDTO
    private lateinit var invalidUserForm: UserFormDTO

    @BeforeEach
    fun setUp() {
        validationService = ValidationService(userRepository)

        validPostForm = PostFormDTO().apply {
            title = "유효한 게시글 제목"
            summary = "유효한 요약"
            content = "유효한 내용입니다."
            images = "valid-image.jpg"
            category_title = "유효한 카테고리"
            tags = "Kotlin,Spring,Test"
        }

        invalidPostForm = PostFormDTO().apply {
            title = ""
            summary = ""
            content = ""
            images = ""
            category_title = ""
            tags = ""
        }

        validUserForm = UserFormDTO().apply {
            username = "validuser"
            password = "validpassword123"
        }

        invalidUserForm = UserFormDTO().apply {
            username = ""
            password = "123"
        }
    }

    @Test
    fun `유효한 게시글 폼 데이터는 검증을 통과한다`() {
        val validationFields: Map<String?, String?> = mapOf(
            "포스트 제목" to validPostForm.title,
            "포스트 요약" to validPostForm.summary,
            "포스트 내용" to validPostForm.content,
            "타이틀 이미지" to validPostForm.images,
            "카테고리 제목" to validPostForm.category_title
        )

        val result = validationService.checkValidForm(validPostForm, validationFields)

        StepVerifier.create(result)
            .expectNext(validPostForm)
            .verifyComplete()
    }

    @Test
    fun `빈 제목을 가진 게시글 폼은 검증에 실패한다`() {
        val validationFields: Map<String?, String?> = mapOf(
            "포스트 제목" to invalidPostForm.title,
            "포스트 요약" to invalidPostForm.summary,
            "포스트 내용" to invalidPostForm.content,
            "타이틀 이미지" to invalidPostForm.images,
            "카테고리 제목" to invalidPostForm.category_title
        )

        try {
            validationService.checkValidForm(invalidPostForm, validationFields).block()
            assert(false) { "예외가 발생해야 함" }
        } catch (e: Exception) {
            assert(e.message!!.contains("다음의 값을 입력해 주세요"))
        }
    }

    @Test
    fun `유효한 사용자 폼 데이터는 검증을 통과한다`() {
        val userValidationFields: Map<String?, String?> = mapOf(
            "사용자명" to validUserForm.username,
            "비밀번호" to validUserForm.password
        )

        val result = validationService.checkValidForm(validUserForm, userValidationFields)

        StepVerifier.create(result)
            .expectNext(validUserForm)
            .verifyComplete()
    }

    @Test
    fun `빈 사용자명은 검증에 실패한다`() {
        val userValidationFields: Map<String?, String?> = mapOf(
            "사용자명" to invalidUserForm.username,
            "비밀번호" to invalidUserForm.password
        )

        try {
            validationService.checkValidForm(invalidUserForm, userValidationFields).block()
            assert(false) { "예외가 발생해야 함" }
        } catch (e: Exception) {
            assert(e.message!!.contains("다음의 값을 입력해 주세요"))
        }
    }

    @Test
    fun `사용자명 중복을 확인할 수 있다`() {
        whenever(userRepository.existsByUsername("validuser")).thenReturn(Mono.just(false))

        val result = validationService.checkValidUsername(validUserForm)

        StepVerifier.create(result)
            .expectNext(validUserForm)
            .verifyComplete()

        verify(userRepository).existsByUsername("validuser")
    }

    @Test
    fun `중복된 사용자명은 검증에 실패한다`() {
        whenever(userRepository.existsByUsername("validuser")).thenReturn(Mono.just(true))

        val result = validationService.checkValidUsername(validUserForm)

        StepVerifier.create(result)
            .expectError()
            .verify()

        verify(userRepository).existsByUsername("validuser")
    }

    @Test
    fun `존재하지 않는 사용자명은 검증에 실패한다`() {
        whenever(userRepository.existsByUsername("nonexistent")).thenReturn(Mono.just(false))

        val nonExistentUserForm = UserFormDTO().apply {
            username = "nonexistent"
            password = "password123"
        }

        val result = validationService.checkNotValidUsername(nonExistentUserForm)

        StepVerifier.create(result)
            .expectError()
            .verify()

        verify(userRepository).existsByUsername("nonexistent")
    }

    @Test
    fun `존재하는 사용자명 확인이 성공한다`() {
        whenever(userRepository.existsByUsername("existinguser")).thenReturn(Mono.just(true))

        val existingUserForm = UserFormDTO().apply {
            username = "existinguser"
            password = "password123"
        }

        val result = validationService.checkNotValidUsername(existingUserForm)

        StepVerifier.create(result)
            .expectNext(existingUserForm)
            .verifyComplete()

        verify(userRepository).existsByUsername("existinguser")
    }

    @Test
    fun `사용자명 존재 여부를 불린값으로 확인할 수 있다`() {
        whenever(userRepository.existsByUsername("testuser")).thenReturn(Mono.just(true))

        val testUserForm = UserFormDTO().apply {
            username = "testuser"
            password = "password123"
        }

        val result = validationService.checkUsernameBoolean(testUserForm)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()

        verify(userRepository).existsByUsername("testuser")
    }

    @Test
    fun `존재하지 않는 사용자명에 대해 false를 반환한다`() {
        whenever(userRepository.existsByUsername("nonexistentuser")).thenReturn(Mono.just(false))

        val nonExistentUserForm = UserFormDTO().apply {
            username = "nonexistentuser"
            password = "password123"
        }

        val result = validationService.checkUsernameBoolean(nonExistentUserForm)

        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()

        verify(userRepository).existsByUsername("nonexistentuser")
    }
} 