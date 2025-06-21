package com.ydh.jigglog.config

import com.ydh.jigglog.domain.dto.CategoryListDTO
import com.ydh.jigglog.domain.entity.Post
import com.ydh.jigglog.domain.entity.Tag
import com.ydh.jigglog.domain.entity.User
import com.ydh.jigglog.repository.*
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.mockito.Mockito
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@Profile("test")
@EnableWebFluxSecurity
class TestConfig {

    @Bean
    @Primary
    fun webClient(): WebClient {
        return WebClient.builder().build()
    }

    @Bean
    @Primary
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager()
    }

    @Bean
    @Primary
    fun testSecurityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf().disable()
            .authorizeExchange().anyExchange().permitAll()
            .and()
            .build()
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

    @Bean
    @Primary
    fun mockCommentRepository(): CommentRepository {
        return Mockito.mock(CommentRepository::class.java)
    }

    @Bean
    @Primary
    fun mockReCommentRepository(): ReCommentRepository {
        return Mockito.mock(ReCommentRepository::class.java)
    }

    @Bean
    @Primary
    fun mockUserRepository(): UserRepository {
        return Mockito.mock(UserRepository::class.java)
    }

    @Bean
    @Primary
    fun mockPostRepository(): PostRepository {
        return Mockito.mock(PostRepository::class.java)
    }

    @Bean
    @Primary
    fun mockCategoryRepository(): CategoryRepository {
        return Mockito.mock(CategoryRepository::class.java)
    }

    @Bean
    @Primary
    fun mockTagRepository(): TagRepository {
        return Mockito.mock(TagRepository::class.java)
    }

    @Bean
    @Primary
    fun mockPostToTagRepository(): PostToTagRepository {
        return Mockito.mock(PostToTagRepository::class.java)
    }

    @Bean
    @Primary
    fun mockIconSetRepository(): IconSetRepository {
        return Mockito.mock(IconSetRepository::class.java)
    }

    @Bean
    @Primary
    fun mockImageUrlRepository(): ImageUrlRepository {
        return Mockito.mock(ImageUrlRepository::class.java)
    }

    @Bean
    @Primary
    fun mockPortfolioRepository(): PortfolioRepository {
        return Mockito.mock(PortfolioRepository::class.java)
    }

    @Bean
    @Primary
    fun mockResumeRepository(): ResumeRepository {
        return Mockito.mock(ResumeRepository::class.java)
    }
} 