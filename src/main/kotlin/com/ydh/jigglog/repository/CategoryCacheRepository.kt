package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.dto.CategoryDTO
import com.ydh.jigglog.domain.dto.CategoryListDTO
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class CategoryCacheRepository(
    private val categoryRepository: CategoryRepository,
    private val redisTemplate: ReactiveRedisTemplate<String?, CategoryListDTO>
) {
    companion object {
        // Cache를 보관할 기간을 정의
        val DAYS_TO_LIVE = 1L
    }
    
    fun findAllAndCaching(): Mono<CategoryListDTO> {
        return redisTemplate.opsForValue().get("categoryAll")
    }

    fun setCategoriesAllAndCaching(categoriesDTO: MutableList<CategoryDTO>): Mono<CategoryListDTO> {
        redisTemplate.opsForValue().set("categoryAll", CategoryListDTO(categories = categoriesDTO), java.time.Duration.ofDays(DAYS_TO_LIVE)).subscribe()
        return redisTemplate.opsForValue().get("categoryAll")
    }

}