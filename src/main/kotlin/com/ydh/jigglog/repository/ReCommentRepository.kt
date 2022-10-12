package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.ReComment
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ReCommentRepository: ReactiveCrudRepository<ReComment, Int> {
    fun findAllByCommentIdIn(commentIds: List<Int>): Flux<ReComment>
}