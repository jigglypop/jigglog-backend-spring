package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.IconSet
import com.ydh.jigglog.domain.entity.ImageUrl
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ImageUrlRepository: ReactiveCrudRepository<ImageUrl, Int> {
    fun findAllByPostIdIn(postIds: List<Int>): Flux<ImageUrl>
    fun findByPostId(postId: Int): Flux<ImageUrl>
}