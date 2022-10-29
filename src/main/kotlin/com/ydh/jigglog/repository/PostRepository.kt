package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.dto.PostInCategoryInDTO
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

    @Query(
        "SELECT " +
                "post.id as id, " +
                "post.title as title, " +
                "post.summary as summary, " +
                "post.images as images, " +
                "post.viewcount as viewcount, " +
                "post.createdat as createdat, " +
                "user.id as userid, " +
                "user.username as username, " +
                "user.imageurl as imageurl, " +
    "( " +
        "SELECT COUNT(comment.id) " +
        "FROM comment " +
        "WHERE comment.postId = post.id " +
    ")  as commentcount, " +
    "( " +
        "SELECT COUNT(post.id) " +
        "FROM post " +
        "WHERE post.categoryId = :categoryId " +
    ") as postcount, " +
    "CEIL( " +
        "( " +
            "SELECT COUNT(post.id) " +
            "FROM post " +
            "WHERE post.categoryId = :categoryId " +
        ") / 8 "  +
    ")  as last " +
    "FROM post " +
    "JOIN user " +
    "ON user.id = post.userId " +
    "WHERE post.categoryId = :categoryId " +
    "LIMIT :limit OFFSET :offset;"
    )
    fun findAllByCategoryId(categoryId: Int, offset: Int, limit: Int? = 8): Flux<PostInCategoryInDTO>

    @Query(
        "SELECT " +
            "post_new.*, " +
            "user.id as userId, " +
            "user.username as username, " +
            "user.imageurl as imageurl, " +
        "( " +
            "SELECT COUNT(comment.id) " +
            "FROM comment " +
            "WHERE comment.postId = post_new.id " +
            ")  as commentcount, " +
        "( " +
            "SELECT COUNT(post_to_tag.id) " +
            "FROM post_to_tag " +
            "WHERE post_to_tag.tagId = :tagId " +
        ") as postcount, " +
        "CEIL( " +
            "( " +
                "SELECT COUNT(post_to_tag.id) " +
                "FROM post_to_tag " +
                "WHERE post_to_tag.tagId = :tagId " +
            ") / 8 " +
        ")  as last " +
        "FROM ( " +
            "SELECT " +
                "post.id as id, " +
                "post.title as title, " +
                "post.summary as summary, " +
                "post.images as images, " +
                "post.viewcount as viewcount, " +
                "post.createdat as createdat, " +
                "post.userid as userid " +
            "FROM post_to_tag " +
            "INNER JOIN post " +
            "WHERE post.id = post_to_tag.postId " +
            "AND post_to_tag.tagId = :tagId " +
    ") as post_new " +
    "JOIN user " +
    "ON user.id = post_new.userId " +
    "LIMIT :limit OFFSET :offset;"
    )
    fun findAllByTagId(tagId: Int, offset: Int, limit: Int? = 8): Flux<PostInCategoryInDTO>
}