package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.IconSet
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface IconSetRepository: ReactiveCrudRepository<IconSet, Int> {
    fun findAllByPostId(postId: Int): Flux<IconSet>
}