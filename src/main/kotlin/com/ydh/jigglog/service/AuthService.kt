package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.domain.entity.User
import com.ydh.jigglog.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class AuthService (
   private val userRepository: UserRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthService::class.java)
    }
    // 유저 생성
    fun createUser(userForm: UserFormDTO): Mono<User> {
        val user = User(
            username = userForm.username,
            hashedPassword = userForm.password
        )
        logger.info(user.username, user.hashedPassword, user.email)
        return userRepository.save(user)
    }
    // 유저 아이디로 가져오기
    fun getUserById(userId: Int): Mono<User> {
        return userRepository.findById(userId).flatMap {
            if (it == null) {
                throw error("유저가 없습니다")
            } else {
                Mono.just(it)
            }
        }
    }
    // 유저 가져오기
    fun getUser(user: User): Mono<User> {
        return userRepository.findById(user.id).flatMap {
            if (it == null) {
                throw error("유저가 없습니다")
            } else {
                Mono.just(it)
            }
        }
    }
    // 유저 이름으로 가져오기
    fun getUserByUsername(username: String): Mono<User> {
        return userRepository.findByUsername(username).flatMap {
            if (it == null) {
                throw error("유저가 없습니다")
            } else {
                Mono.just(it)
            }
        }
    }
}