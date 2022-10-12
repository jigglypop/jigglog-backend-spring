package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.Tag
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TagRepository: ReactiveCrudRepository<Tag, Int> {
    fun findAllByTitleIn(title: List<String>): Flux<Tag>

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