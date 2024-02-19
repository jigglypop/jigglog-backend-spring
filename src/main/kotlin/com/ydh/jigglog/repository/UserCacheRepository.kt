package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.User
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class UserCacheRepository(
    private val userRepository: UserRepository,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, User>
) {
    companion object {
        // Cache를 보관할 기간을 정의
        val DAYS_TO_LIVE = 1L

    }

    fun findByNameWithCaching(username: String): Mono<User> {
        val setUserMono: Mono<User?> = userRepository.findByUsername(username)
            .doOnSuccess {
                it?.let {
                    reactiveRedisTemplate.opsForValue().set(username, it, java.time.Duration.ofDays(DAYS_TO_LIVE))
                        .subscribe()
                }
            }.onErrorResume {
                Mono.empty()
            }
        return reactiveRedisTemplate.opsForValue().get(username).switchIfEmpty(setUserMono)
    }


}