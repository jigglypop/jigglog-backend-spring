package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.domain.entity.User
import com.ydh.jigglog.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class SecurityServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var serverRequest: ServerRequest

    @Mock
    private lateinit var serverHttpRequest: ServerHttpRequest

    private lateinit var securityService: SecurityService

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        securityService = SecurityService(
            "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=",
            userRepository
        )
        securityService.owner = "admin"

        testUser = User(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            hashedPassword = "hashedPassword123",
            imageUrl = "profile.jpg",
            githubUrl = "",
            summary = "테스트 사용자"
        )
    }

    @Test
    fun `JWT 토큰을 생성할 수 있다`() {
        val result = securityService.generateToken(testUser)

        StepVerifier.create(result)
            .expectNextMatches { token ->
                token.isNotEmpty() && token.contains(".")
            }
            .verifyComplete()
    }

    @Test
    fun `유효한 토큰을 검증할 수 있다`() {
        val token = securityService.generateToken(testUser).block()
        val headers = mock<ServerRequest.Headers>()
        val httpHeaders = mock<org.springframework.http.HttpHeaders>()
        
        whenever(headers.asHttpHeaders()).thenReturn(httpHeaders)
        whenever(httpHeaders.getFirst("Authorization")).thenReturn("Bearer $token")

        val result = securityService.checkValidToken(headers)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `잘못된 토큰 검증시 예외가 발생한다`() {
        val headers = mock<ServerRequest.Headers>()
        val httpHeaders = mock<org.springframework.http.HttpHeaders>()
        
        whenever(headers.asHttpHeaders()).thenReturn(httpHeaders)
        whenever(httpHeaders.getFirst("Authorization")).thenReturn("Bearer invalid-token")

        try {
            securityService.checkValidToken(headers).block()
            assert(false) { "예외가 발생해야 함" }
        } catch (e: Exception) {
            assert(e.message == "올바른 토큰이 아닙니다.")
        }
    }

    @Test
    fun `Bearer 토큰 형식을 검증할 수 있다`() {
        val headers = mock<ServerRequest.Headers>()
        val httpHeaders = mock<org.springframework.http.HttpHeaders>()
        
        whenever(headers.asHttpHeaders()).thenReturn(httpHeaders)
        whenever(httpHeaders.getFirst("Authorization")).thenReturn("Bearer valid-token")

        val result = securityService.checkValidHeader(headers)

        StepVerifier.create(result)
            .expectNext("valid-token")
            .verifyComplete()
    }

    @Test
    fun `잘못된 토큰 형식 검증시 예외가 발생한다`() {
        val headers = mock<ServerRequest.Headers>()
        val httpHeaders = mock<org.springframework.http.HttpHeaders>()
        
        whenever(headers.asHttpHeaders()).thenReturn(httpHeaders)
        whenever(httpHeaders.getFirst("Authorization")).thenReturn("Invalid token-format")

        val result = securityService.checkValidHeader(headers)

        StepVerifier.create(result)
            .expectError(Exception::class.java)
            .verify()
    }

    @Test
    fun `서버 요청에서 로그인된 사용자를 가져올 수 있다`() {
        val token = securityService.generateToken(testUser).block()
        val headers = mock<ServerRequest.Headers>()
        val httpHeaders = mock<org.springframework.http.HttpHeaders>()
        
        whenever(serverRequest.headers()).thenReturn(headers)
        whenever(headers.asHttpHeaders()).thenReturn(httpHeaders)
        whenever(httpHeaders.getFirst("Authorization")).thenReturn("Bearer $token")
        whenever(userRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser))

        val result = securityService.getLoggedInUser(serverRequest)

        StepVerifier.create(result)
            .expectNextMatches { user ->
                user.id == 1 &&
                user.username == "testuser" &&
                user.hashedPassword == ""
            }
            .verifyComplete()

        verify(userRepository).findByUsername("testuser")
    }

    @Test
    fun `Authorization 헤더가 없으면 예외가 발생한다`() {
        val headers = mock<ServerRequest.Headers>()
        val httpHeaders = mock<org.springframework.http.HttpHeaders>()
        
        whenever(serverRequest.headers()).thenReturn(headers)
        whenever(headers.asHttpHeaders()).thenReturn(httpHeaders)
        whenever(httpHeaders.getFirst("Authorization")).thenReturn(null)

        try {
            securityService.getLoggedInUser(serverRequest).block()
            assert(false) { "예외가 발생해야 함" }
        } catch (e: Exception) {
            assert(e.message == "로그인이 필요한 서비스입니다.")
        }
    }

    @Test
    fun `관리자 권한을 확인할 수 있다`() {
        securityService.owner = "testuser"
        val ownerUser = User(
            id = 1,
            username = "testuser",
            email = "owner@example.com",
            hashedPassword = "hashedPassword",
            imageUrl = "owner.jpg",
            githubUrl = "",
            summary = "소유자"
        )

        val result = securityService.isOwner(ownerUser)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `관리자가 아닌 사용자의 권한 확인시 예외가 발생한다`() {
        securityService.owner = "admin"
        val nonOwnerUser = User(
            id = 2,
            username = "normaluser",
            email = "normal@example.com",
            hashedPassword = "hashedPassword",
            imageUrl = "normal.jpg",
            githubUrl = "",
            summary = "일반 사용자"
        )

        val result = securityService.isOwner(nonOwnerUser)

        StepVerifier.create(result)
            .expectError()
            .verify()
    }

    @Test
    fun `작성자 권한을 확인할 수 있다`() {
        val result = securityService.checkIsOwner(1, 1)

        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `작성자가 아닌 경우 예외가 발생한다`() {
        val result = securityService.checkIsOwner(1, 2)

        StepVerifier.create(result)
            .expectError()
            .verify()
    }
} 