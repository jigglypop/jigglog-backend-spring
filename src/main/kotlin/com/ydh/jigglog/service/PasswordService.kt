package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.domain.entity.User
import com.ydh.jigglog.repository.UserRepository
import com.ydh.jigglog.exception.InvalidPasswordException

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PasswordService (
    @param:Value("\${spring.datasource.salt}") private val salt: String,
    @Autowired private val userRepository: UserRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PasswordService::class.java)
        private val passwordEncoder = BCryptPasswordEncoder()
    }
    
    // 패스워드 encode
    fun encode(hashedPassword: String): String {
        return passwordEncoder.encode(hashedPassword)
    }
    
    // 패스워드 체크
    fun matches(encodedPassword: String, rawPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
    
    // 패스워드 해싱
    fun changeHashedPassword(userForm: UserFormDTO): Mono<UserFormDTO> {
        logger.debug("Encoding password for user: ${userForm.username}")
        return Mono.just(userForm).map { form ->
            form.apply {
                this.password = encode(this.password!!)
            }
        }
    }
    
    // 패스워드 매치 체크 후 삭제
    fun checkPassword(orgUser: User, password: String): Mono<User?> {
        return if (matches(orgUser.hashedPassword!!, password)) {
            logger.debug("Password matched for user: ${orgUser.username}")
            Mono.just(orgUser.apply {
                this.hashedPassword = ""
            })
        } else {
            logger.warn("Invalid password attempt for user: ${orgUser.username}")
            Mono.error(InvalidPasswordException("비밀번호가 일치하지 않습니다"))
        }
    }
}