package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.Comment
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface CommentRepository: ReactiveCrudRepository<Comment, Int> {
    fun findAllByPostId(postId: Int): Flux<Comment>
}