package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.Post
import com.ydh.jigglog.domain.Tag
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TagRepository: ReactiveCrudRepository<Tag, Int> {

    @Query(
        "SELECT * " +
        "FROM post " +
        "JOIN (SELECT * FROM post_to_tag WHERE :tagId = post_to_tag.tagId) as PostToTag " +
        "WHERE post.id = PostToTag.postId"
    )
    fun findPostsByTagId(tagId: Int): Flux<Post>

    @Query(
        "SELECT distinct(post.tagId) as id, post.title " +
        "FROM ( " +
                "SELECT post_to_tag.id, post_to_tag.postId, post_to_tag.tagId, tag.title " +
                "FROM tag " +
                "LEFT OUTER JOIN post_to_tag " +
                "ON post_to_tag.tagId = tag.id " +
            ") post " +
        "WHERE post.id is not null "
    )
    fun findTagsAllContainPost(): Flux<Tag>
}