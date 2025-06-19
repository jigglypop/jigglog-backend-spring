package com.ydh.jigglog.config

import com.ydh.jigglog.domain.dto.CategoryListDTO
import com.ydh.jigglog.domain.entity.Post
import com.ydh.jigglog.domain.entity.Tag
import com.ydh.jigglog.domain.entity.User
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.mockito.Mockito

@Configuration
@Profile("test")
class TestConfig {

    @Bean
    @Primary
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager()
    }

    @Bean("userReactiveRedisTemplate")
    @Primary
    @Suppress("UNCHECKED_CAST")
    fun mockUserReactiveRedisTemplate(): ReactiveRedisTemplate<String, User> {
        return Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, User>
    }

    @Bean("resumeReactiveRedisTemplate")
    @Primary
    @Suppress("UNCHECKED_CAST")
    fun mockResumeReactiveRedisTemplate(): ReactiveRedisTemplate<String, Post> {
        return Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, Post>
    }

    @Bean("categoryReactiveRedisTemplate")
    @Primary
    @Suppress("UNCHECKED_CAST")
    fun mockCategoryReactiveRedisTemplate(): ReactiveRedisTemplate<String, CategoryListDTO> {
        return Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, CategoryListDTO>
    }

    @Bean("tagReactiveRedisTemplate")
    @Primary
    @Suppress("UNCHECKED_CAST")
    fun mockTagReactiveRedisTemplate(): ReactiveRedisTemplate<String, Tag> {
        return Mockito.mock(ReactiveRedisTemplate::class.java) as ReactiveRedisTemplate<String, Tag>
    }
} 