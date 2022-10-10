package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.Category
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface CategoryRepository: ReactiveCrudRepository<Category, Int> {
    fun existsByTitle(title: String): Mono<Boolean>
    fun findByTitle(title: String): Mono<Category>
}