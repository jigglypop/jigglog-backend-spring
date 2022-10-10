package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.domain.entity.User
import com.ydh.jigglog.repository.UserRepository

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class PasswordService (
    @param:Value("\${spring.datasource.salt}") private val salt: String,
    @Autowired private val userRepository: UserRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PasswordService::class.java)
    }
    // 패스워드 encode
    fun encode(hashedPassword: String): String {
        return BCryptPasswordEncoder().encode(hashedPassword)
    }
    // 패스워드 체크
    fun matches( encodedPassword: String, rawPassword: String): Boolean {
        return BCryptPasswordEncoder().matches(rawPassword, encodedPassword)
    }
    // 패스워드 해싱
    fun changeHashedPassword(userForm: UserFormDTO): Mono<UserFormDTO> {
        logger.info("패스워드 해싱")
        return Mono.just(userForm).flatMap {
                Mono.just(it.apply {
                   this.password = encode(this.password!!)
                })
            }
    }
    // 패스워드 매치 체크 후 삭제
    fun checkPassword(orgUser: User, password: String): Mono<User?> {
        return if (matches(orgUser.hashedPassword!!, password)) {
            Mono.just(orgUser.apply {
                this.hashedPassword = ""
            })
        } else {
            Mono.error(Exception("비밀번호가 일치하지 않습니다."))
        }
    }
}