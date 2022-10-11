package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.Post
import com.ydh.jigglog.domain.entity.Tag
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PostRepository: ReactiveCrudRepository<Post, Int> {
    @Query(
        "SELECT * " +
        "FROM tag " +
        "JOIN (SELECT * FROM post_to_tag WHERE :postId = post_to_tag.postId) as PostToTag " +
        "WHERE tag.id = PostToTag.tagId"
    )
    fun findTagsByPostId(postId: Int): Flux<Tag>
}