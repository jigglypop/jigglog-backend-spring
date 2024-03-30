package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.Post
import com.ydh.jigglog.domain.entity.User
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class ResumeCacheRepository(
    private val postRepository: PostRepository,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Post>
) {
    companion object {
        // Cache를 보관할 기간을 정의
        val DAYS_TO_LIVE = 30L

    }

    fun findResumeAndCaching(): Mono<Post> {
        val setResumeMono: Mono<Post> = postRepository.findById(1)
            .doOnSuccess {
                it?.let {
                    reactiveRedisTemplate.opsForValue().set("resume", it, java.time.Duration.ofDays(DAYS_TO_LIVE))
                        .subscribe()
                }
            }.onErrorResume {
                Mono.empty()
            }
        return reactiveRedisTemplate.opsForValue().get("resume").switchIfEmpty(setResumeMono)
    }


}