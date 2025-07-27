package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.domain.entity.User
import com.ydh.jigglog.repository.UserCacheRepository
import com.ydh.jigglog.repository.UserRepository
import com.ydh.jigglog.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuthService (
   private val userRepository: UserRepository,
   private val userCacheRepository: UserCacheRepository
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
        return userRepository.save(user)
            .doOnSuccess { logger.info("Created new user: ${it.username}") }
            .map { savedUser -> 
                // 응답에서 비밀번호 제거
                createSafeUser(savedUser)
            }
    }
    
    // 유저 아이디로 가져오기
    fun getUserById(userId: Int): Mono<User> {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(UserNotFoundException("유저를 찾을 수 없습니다. ID: $userId")))
            .map { user -> createSafeUser(user) }
    }
    
    // 유저 가져오기
    fun getUser(user: User): Mono<User> {
        return userRepository.findById(user.id)
            .switchIfEmpty(Mono.error(UserNotFoundException("유저를 찾을 수 없습니다. ID: ${user.id}")))
            .map { foundUser -> createSafeUser(foundUser) }
    }
    
    // 유저 이름으로 가져오기
    fun getUserByUsername(username: String): Mono<User> {
        return userCacheRepository.findByNameWithCaching(username)
            .doOnNext { logger.debug("Found user from cache: $username") }
    }
    
    // 유저 이름으로 가져오기 (안전한 버전 - 비밀번호 제거)
    fun getUserByUsernameSafe(username: String): Mono<User> {
        return getUserByUsername(username)
            .map { user -> createSafeUser(user) }
    }
    
    // 비밀번호를 제거한 안전한 User 객체 생성
    private fun createSafeUser(user: User): User {
        return User(
            id = user.id,
            username = user.username,
            email = user.email,
            hashedPassword = "", // 비밀번호는 빈 문자열로 설정
            imageUrl = user.imageUrl,
            githubUrl = user.githubUrl,
            summary = user.summary
        )
    }
}