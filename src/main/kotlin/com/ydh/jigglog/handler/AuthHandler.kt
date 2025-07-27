package com.ydh.jigglog.handler

import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.domain.entity.User
import com.ydh.jigglog.service.AuthService
import com.ydh.jigglog.service.PasswordService
import com.ydh.jigglog.service.SecurityService
import com.ydh.jigglog.service.ValidationService
import com.ydh.jigglog.util.ResponseHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class AuthHandler(
    @param:Value("\${spring.datasource.secretuser}") private val secretuser: String,
    @param:Value("\${spring.datasource.secretpassword}") private val secretpassword: String,
    @Autowired val securityService: SecurityService,
    @Autowired val passwordService: PasswordService,
    @Autowired val validationService: ValidationService,
    @Autowired val authService: AuthService,
    @Autowired val redisTemplate: ReactiveRedisTemplate<String, User>
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthHandler::class.java)
    }
    
    fun test(req: ServerRequest): Mono<ServerResponse> = 
        authService.getUserById(1)
            .flatMap { user -> ResponseHelper.success(user) }
            .onErrorResume { error -> ResponseHelper.errorResponse(error) }
    
    // 회원가입
    fun register(req: ServerRequest): Mono<ServerResponse> = 
        req.bodyToMono(UserFormDTO::class.java)
            // 병렬 실행 : 폼 체크, 유저 중복 체크
            .flatMap { userForm ->
                validationService.checkValidForm<UserFormDTO>(
                    userForm, 
                    mapOf("유저 이름" to userForm.username, "비밀번호" to userForm.password)
                )
            }
            // 유저 이름 있는지 확인
            .flatMap { userForm ->
                validationService.checkValidUsername(userForm)
            }
            // 해싱된 비밀번호로 바꾸기
            .flatMap { userForm ->
                passwordService.changeHashedPassword(userForm)
            }
            // 저장
            .flatMap { userForm ->
                authService.createUser(userForm)
            }
            // 프로필 생성, 토큰 생성
            .flatMap { user ->
                Mono.zip(
                    securityService.generateToken(user), 
                    Mono.just(user)
                )
            }
            // 응답
            .flatMap { tuple ->
                val token = tuple.t1
                val user = tuple.t2
                ResponseHelper.successWithToken(token, user)
            }
            .onErrorResume { error -> 
                ResponseHelper.errorResponse(error)
            }

    // 로그인
    fun login(req: ServerRequest): Mono<ServerResponse> = 
        req.bodyToMono(UserFormDTO::class.java)
            // 병렬 실행 : 폼 체크, 패스워드 체크
            .flatMap { userForm ->
                validationService.checkValidForm<UserFormDTO>(
                    userForm, 
                    mapOf("유저 이름" to userForm.username, "비밀번호" to userForm.password)
                )
            }
            // 해당 이름 유저 체크
            .flatMap { userForm ->
                validationService.checkNotValidUsername(userForm)
            }
            .flatMap { userForm ->
                Mono.zip(
                    authService.getUserByUsername(userForm.username!!),
                    Mono.just(userForm)
                )
            }
            .flatMap { tuple ->
                val user = tuple.t1
                val userForm = tuple.t2
                passwordService.checkPassword(user, userForm.password!!)
            }
            .flatMap { user ->
                // checkPassword에서 이미 비밀번호가 제거된 user를 반환함
                Mono.zip(
                    securityService.generateToken(user!!), 
                    Mono.just(user)
                )
            }
            // 응답
            .flatMap { tuple ->
                val token = tuple.t1
                val user = tuple.t2
                ResponseHelper.successWithToken(token, user)
            }
            .onErrorResume { error ->
                ResponseHelper.errorResponse(error)
            }
            
    // 코멘트
    fun comment(req: ServerRequest): Mono<ServerResponse> = 
        req.bodyToMono(UserFormDTO::class.java)
            // 폼 채우기
            .flatMap { userForm ->
                if (userForm.username.isNullOrBlank() && userForm.password.isNullOrBlank()) {
                    userForm.username = secretuser
                    userForm.password = secretpassword
                }
                Mono.just(userForm)
            }
            // 병렬 실행 : 유저이름 존재 여부 확인
            .flatMap { userForm ->
                Mono.zip(
                    validationService.checkUsernameBoolean(userForm),
                    Mono.just(userForm)
                )
            }
            .flatMap { tuple ->
                val existedUsername = tuple.t1
                val userForm = tuple.t2
                // 유저이름 있음 -> 로그인 로직
                if (existedUsername) {
                    authService.getUserByUsername(userForm.username!!)
                        .flatMap { user ->
                            passwordService.checkPassword(user, userForm.password!!)
                        }
                // 유저이름 없음 -> 회원가입 로직
                } else {
                    passwordService.changeHashedPassword(userForm)
                        .flatMap { hashedUserForm ->
                            authService.createUser(hashedUserForm)
                        }
                }
            }
            // 토큰 생성
            .flatMap { user ->
                Mono.zip(
                    securityService.generateToken(user!!), 
                    Mono.just(user)
                )
            }
            // 응답
            .flatMap { tuple ->
                val token = tuple.t1
                val user = tuple.t2
                ResponseHelper.successWithToken(token, user)
            }
            .onErrorResume { error ->
                ResponseHelper.errorResponse(error)
            }
            
    // 체크
    fun check(req: ServerRequest): Mono<ServerResponse> =
        Mono.just(req.headers())
            // 병렬 실행 : 토큰 유효성, 토큰 Bearer 형식 체크
            .flatMap { headers ->
                Mono.zip(
                    securityService.checkValidHeader(headers),
                    securityService.checkValidToken(headers)
                )
            }
            .flatMap { tuple ->
                val validHeader = tuple.t1
                securityService.parseJwtToken(validHeader)
                    .toMono()
            }
            .flatMap { claims ->
                authService.getUserByUsernameSafe(claims.subject)
            }
            .flatMap { user ->
                ResponseHelper.success(user)
            }
            .onErrorResume { error ->
                ResponseHelper.errorResponse(error)
            }
}



