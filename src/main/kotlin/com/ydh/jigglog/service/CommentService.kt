package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.*
import com.ydh.jigglog.domain.entity.Comment
import com.ydh.jigglog.repository.CommentRepository
import com.ydh.jigglog.repository.ReCommentRepository
import com.ydh.jigglog.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class CommentService (
    @Autowired private val commentRepository: CommentRepository,
    @Autowired private val recommentRepository: ReCommentRepository,
    @Autowired private val userRepository: UserRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CommentService::class.java)
    }
    // 코멘트 생성
    fun createComment(commentForm: CommentFormDTO, userId: Int, postId: Int): Mono<Comment> {
        return commentRepository.save(
            Comment(
                content = commentForm.content,
                userId = userId,
                postId = postId
            )
        )
    }
    // 코멘트 삭제
    fun deleteComment(commentId: Int): Mono<Boolean> {
        return commentRepository.deleteById(commentId).thenReturn(true)

    }
    // 단일 코멘트 가져오기
    fun getComment(commentId: Int): Mono<Comment> {
        return commentRepository.existsById(commentId)
            .flatMap {  isExist ->
                if (isExist) {
                    commentRepository.findById(commentId)
                } else {
                    throw error("댓글이 없습니다")
                }
            }
    }

    // 포스트로 모두 가져오기
    fun getCommentByPostId(postId: Int): Mono<List<CommentDTO>> {
        return Mono.just(postId).flatMap { postId ->
            commentRepository.findAllByPostIdAndUser(postId).collectList().toMono()
        }.flatMap {
            val comment_idx = mutableMapOf<Int, CommentDTO>()
            for (commentsAll in it) {
                val recomment = ReCommentDTO(
                    id = commentsAll.recomment_id,
                    content = commentsAll.recomment_content,
                    createdAt = commentsAll.recomment_createdat,
                    user = UserDTO(
                        id = commentsAll.recomment_userid,
                        username = commentsAll.recomment_username,
                        email = commentsAll.recomment_email,
                        imageUrl = commentsAll.recomment_imageurl,
                        githubUrl = commentsAll.recomment_githuburl,
                        summary = commentsAll.recomment_summary,
                    )
                )
                if (commentsAll.comment_id in comment_idx) {
                    comment_idx[commentsAll.comment_id]?.recomments?.add(recomment)
                } else {
                    val comment = CommentDTO(
                        id = commentsAll.comment_id,
                        content = commentsAll.comment_content,
                        createdAt = commentsAll.comment_createdat,
                        recomments = mutableListOf<ReCommentDTO>(),
                        user = UserDTO(
                            id = commentsAll.comment_userid,
                            username = commentsAll.comment_username,
                            hashedPassword = "",
                            email = commentsAll.comment_email,
                            imageUrl = commentsAll.comment_imageurl,
                            githubUrl = commentsAll.comment_githuburl,
                            summary = commentsAll.comment_summary,
                        )
                    )
                    comment.recomments.add(recomment)
                    comment_idx[commentsAll.comment_id] = comment
                }
            }
            val results = mutableListOf<CommentDTO>()
            for (i in comment_idx.keys) {
                results.add(comment_idx[i]!!)
            }
            results.toMono()
        }
    }

}