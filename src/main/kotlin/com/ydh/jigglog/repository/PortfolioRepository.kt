package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.dto.PortfolioDTO
import com.ydh.jigglog.domain.entity.Post
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PortfolioRepository: ReactiveCrudRepository<Post, Int> {
    fun findAllByCategoryId(categoryId: Int): Flux<Post>
}