package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.dto.CommentsAllDTO
import com.ydh.jigglog.domain.entity.Comment
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface CommentRepository: ReactiveCrudRepository<Comment, Int> {
    fun findAllByPostId(postId: Int): Flux<Comment>

    @Query(
        "SELECT " +
                "commentsAll.*, " +
                "recommentsAll.* " +
            "FROM( " +
                "SELECT " +
                    "user.username as comment_username, " +
                    "user.email as comment_email, " +
                    "user.imageUrl as comment_imageUrl, " +
                    "user.githubUrl as comment_githubUrl, " +
                    "user.summary as comment_summary, " +
                    "comment_tb.* " +
                "FROM ( " +
                    "SELECT " +
                        "comment.id as comment_id, " +
                        "comment.content as comment_content, " +
                        "comment.createdAt as comment_createdAt, " +
                        "comment.userId as comment_userId, " +
                        "comment.postId as comment_postId " +
                    "FROM comment " +
                    "WHERE comment.postId = :postId " +
                ") as comment_tb " +
                "JOIN user " +
                "ON user.id = comment_userId " +
            ") as commentsAll " +
            "LEFT OUTER JOIN ( " +
                "SELECT " +
                    "user.username as recomment_username, " +
                    "user.email as recomment_email, " +
                    "user.imageUrl as recomment_imageUrl, " +
                    "user.githubUrl as recomment_githubUrl, " +
                    "user.summary as recomment_summary, " +
                    "recomment_tb.* " +
                "FROM ( " +
                "SELECT " +
                    "recomment.id as recomment_id, " +
                    "recomment.content as recomment_content, " +
                    "recomment.createdAt as recomment_createdAt, " +
                    "recomment.userId as recomment_userId, " +
                    "recomment.commentId as recomment_commentId " +
                "FROM recomment " +
                ") as recomment_tb " +
                "JOIN user " +
                "ON user.id = recomment_userId " +
            ") as recommentsAll " +
        "ON comment_id = recomment_commentId "
    )
    fun findAllByPostIdAndUser(postId: Int): Flux<CommentsAllDTO>

}