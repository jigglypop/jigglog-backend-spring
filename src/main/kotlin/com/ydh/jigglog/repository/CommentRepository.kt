package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.dto.EveryCommentDTO
import com.ydh.jigglog.domain.entity.Comment
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface CommentRepository: ReactiveCrudRepository<Comment, Int> {
    fun findAllByPostId(postId: Int): Flux<Comment>

    @Query(
    "SELECT comments.id as id, " +
        "comments.content as content, " +
        "comments.createdat as createdat, " +
        "comments.userid as userid, " +
        "comments.postid as postid, " +
        "user.username as username, " +
        "user.hashedpassword as hashedpassword, " +
        "user.email as email, " +
        "user.imageurl as imageurl, " +
        "user.githuburl as githuburl, " +
        "user.summary as summary " +
            "FROM ( " +
                "SELECT * " +
                "FROM comment " +
                "WHERE comment.postId = :postId " +
            ") as comments " +
            "JOIN user " +
        "ON user.id = comments.userId ")
    fun findAllByPostIdAndUser(postId: Int): Flux<EveryCommentDTO>
}