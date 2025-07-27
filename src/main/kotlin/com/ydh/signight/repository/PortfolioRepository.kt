package com.ydh.signight.repository

import com.ydh.signight.domain.dto.PortfolioDTO
import com.ydh.signight.domain.entity.Post
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PortfolioRepository: ReactiveCrudRepository<Post, Int> {
    fun findAllByCategoryId(categoryId: Int): Flux<Post>
}