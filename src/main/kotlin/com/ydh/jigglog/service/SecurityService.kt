package com.ydh.jigglog.service

import com.ydh.jigglog.domain.entity.User
import com.ydh.jigglog.repository.UserRepository
import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Service
class SecurityService(
    @param:Value("\${spring.datasource.jwt-secret}") private val jwt_secret: String,
    @Autowired private val userRepository: UserRepository,
    ) {
    // JWT_SECRET
    private val JWT_SECRET = jwt_secret
    // 토큰 유효시간
    private val JWT_EXPIRATION_MS = 604800000

    @Value("\${spring.datasource.owner}")
    lateinit var owner: String
    // jwt 토큰 생성
    fun generateToken(user: User): Mono<String> {
        val now = Date()
        val expiryDate = Date(now.getTime() + JWT_EXPIRATION_MS)
        val build = Jwts.builder()
            // jwt 토큰 인자값
            .setSubject(user.username)  // 사용자 이름
            .setIssuedAt(Date()) // 현재 시간 기반으로 생성
            .setExpiration(expiryDate) // 만료 시간 세팅
            .signWith(SignatureAlgorithm.HS256, JWT_SECRET) // (6)
            .compact()
        return Mono.just(build)
    }
    // jwt 토큰 파싱
    fun parseJwtToken(token: String?): Claims {
        return Jwts.parser()
            .setSigningKey(JWT_SECRET) // (3)
            .parseClaimsJws(token)
            .body
    }
    // 토큰의 유효성 + 만료일자 확인
    fun checkValidToken(header: ServerRequest.Headers): Mono<Boolean> {
        return try {
            val token = header.asHttpHeaders().getFirst("Authorization")?.replace("Bearer ", "")
            val claims = Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token)
            Mono.just(true)
        } catch (e: Exception) {
            throw Exception("올바른 토큰이 아닙니다.")
        }
    }
    // 토큰의 받는 형식(Bearer) 검사
    fun checkValidHeader(mono: ServerRequest.Headers): Mono<String?> {
        return Mono.just(mono).flatMap {
            val tokens = it.asHttpHeaders().getFirst("Authorization")?.split(" ")
            if (tokens?.get(0).equals("Bearer")) {
                Mono.just(tokens?.get(1) ?: "")
            } else {
                Mono.error(Exception("토큰 형식이 올바르지 않습니다."))
            }
        }
    }
    // 로그인 여부 확인 후 유저 가져오기
    fun getLoggedInUser(req: ServerRequest): Mono<User> {
        return try {
            val token = req.headers().asHttpHeaders().getFirst("Authorization")?.replace("Bearer ", "")
            val username = Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(token).body.subject
            userRepository.findByUsername(username).flatMap {
                it.apply {
                    this.hashedPassword = ""
                }.toMono()
            }
        } catch (e: Exception) {
            throw Exception("로그인이 필요한 서비스입니다.")
        }
    }
    // 관리자 체크
    fun isOwner(user: User): Mono<Boolean> {
        return Mono.just(user).flatMap {
            if (it.username != owner) {
                throw error("관리자가 아닙니다")
            } else {
                true.toMono()
            }
        }
    }
    // 객체 유저 체크
    fun checkIsOwner(userId: Int, writer: Int): Mono<Boolean> {
        return writer.toMono().flatMap {
            if (it == userId) {
                true.toMono()
            } else {
                throw error("작성자가 아닙니다")
            }
        }
    }
}