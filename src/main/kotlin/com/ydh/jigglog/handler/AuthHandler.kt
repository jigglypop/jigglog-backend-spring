package com.ydh.jigglog.handler

import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.domain.entity.User
import com.ydh.jigglog.service.AuthService
import com.ydh.jigglog.service.PasswordService
import com.ydh.jigglog.service.SecurityService
import com.ydh.jigglog.service.ValidationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class AuthHandler(
    @Autowired val securityService: SecurityService,
    @Autowired val passwordService: PasswordService,
    @Autowired val validationService: ValidationService,
    @Autowired val authService: AuthService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthHandler::class.java)
    }
    fun test(req: ServerRequest) = req.bodyToMono(User::class.java)
        .flatMap {
            authService.getUserById(1)
        }
        .flatMap {
            ok().header("Authorization", "").body(
                Mono.just(it)
            )
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                Mono.just(it)
            )
        }
    // 회원가입
    fun register(req: ServerRequest) = req.bodyToMono(UserFormDTO::class.java)
        // 병렬 실행 : 폼 체크, 유저 중복 체크
        .flatMap {
            validationService.checkValidForm<UserFormDTO>(it, mapOf("유저 이름" to it.username, "비밀번호" to it.password ))
        // 유저 이름 있는지 확인
        }.flatMap {
            validationService.checkValidUsername(it)
        // 해싱된 비밀번호로 바꾸기
        }.flatMap {
            passwordService.changeHashedPassword(it)
        // 저장
        }.flatMap {
            authService.createUser(it)
        // 프로필 생성, 토큰 생성
        }.flatMap {
            Mono.zip(securityService.generateToken(it), authService.getUser(it))
            // 응답
        }.flatMap {
            ok().header("token", "Bearer " + it.t1).body(
                Mono.just(it.t2)
            )
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                Mono.just(it)
            )
        }

    // 로그인
    fun login(req: ServerRequest) = req
        .bodyToMono(UserFormDTO::class.java)
        // 병렬 실행 : 폼 체크, 패스워드 체크
        .flatMap {
            validationService.checkValidForm<UserFormDTO>(it, mapOf("유저 이름" to it.username, "비밀번호" to it.password ))
        }.flatMap { userForm ->
            Mono.just(userForm)
                .flatMap { userForm ->
                    authService.getUserByUsername(userForm.username!!)
                }.flatMap { user ->
                    passwordService.checkPassword(user, userForm.password!!)
                }
        //  토큰 생성
        }.flatMap { user ->
            Mono.zip(securityService.generateToken(user!!), user.toMono())
            // 응답
        }.flatMap {
            ok().header("token", "Bearer " + it.t1).body(
                Mono.just(it.t2)
            )
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                Mono.just(it)
            )
        }

    // 체크
    fun check(req: ServerRequest) =
        Mono.just(req.headers())
                // 병렬 실행 : 토큰 유효성, 토큰 Bearer 형식 체크
            .flatMap {
                Mono.zip(
                    securityService.checkValidHeader(it),
                    securityService.checkValidToken(it)
                )
            }.flatMap {
                securityService.parseJwtToken(it.t1).toMono()
            }.flatMap {
                authService.getUserByUsername(it.subject).toMono()
            }.flatMap {
                ok().body(it.toMono())
            }.onErrorResume(Exception::class.java) {
                badRequest().body(
                    Mono.just(it)
                )
            }
}



