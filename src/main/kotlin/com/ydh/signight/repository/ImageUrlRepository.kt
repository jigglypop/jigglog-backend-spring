package com.ydh.signight.repository

import com.ydh.signight.domain.entity.IconSet
import com.ydh.signight.domain.entity.ImageUrl
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ImageUrlRepository: ReactiveCrudRepository<ImageUrl, Int> {
    fun findAllByPostIdIn(postIds: List<Int>): Flux<ImageUrl>
    fun findByPostId(postId: Int): Flux<ImageUrl>
}