package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.dto.EveryReCommentDTO
import com.ydh.jigglog.domain.entity.ReComment
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ReCommentRepository: ReactiveCrudRepository<ReComment, Int> {
    fun findAllByCommentIdIn(commentIds: List<Int>): Flux<ReComment>

    @Query(
        "SELECT recomments.id as id, " +
            "recomments.content as content, " +
            "recomments.createdat as createdat, " +
            "recomments.userid as userid, " +
            "recomments.commentid as commentid, " +
            "user.username as username, " +
            "user.hashedpassword as hashedpassword, " +
            "user.email as email, " +
            "user.imageurl as imageurl, " +
            "user.githuburl as githuburl, " +
            "user.summary as summary " +
                "FROM ( " +
                    "SELECT * " +
                    "FROM ( " +
                        "SELECT id as originalId " +
                        "FROM comment " +
                        "WHERE comment.postId = :postId " +
                    ") as commentsAll " +
                    "JOIN recomment " +
                    "ON recomment.commentId = commentsAll.originalId " +
                ") as recomments " +
            "JOIN user " +
        "ON user.id = recomments.userId ")
    fun findEveryRecomments(postId: Int): Flux<EveryReCommentDTO>
}