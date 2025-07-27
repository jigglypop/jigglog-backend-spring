package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.*
import com.ydh.jigglog.domain.entity.Comment
import com.ydh.jigglog.repository.CommentRepository
import com.ydh.jigglog.repository.ReCommentRepository
import com.ydh.jigglog.repository.UserRepository
import com.ydh.jigglog.exception.CommentNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
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
        .doOnSuccess { 
            logger.info("Created comment for post $postId by user $userId")
        }
    }
    
    // 코멘트 삭제
    fun deleteComment(commentId: Int): Mono<Boolean> {
        return commentRepository.deleteById(commentId)
            .thenReturn(true)
            .doOnSuccess { 
                logger.info("Deleted comment with id: $commentId")
            }
            .doOnError { error ->
                logger.error("Failed to delete comment with id: $commentId", error)
            }
    }
    
    // 단일 코멘트 가져오기
    fun getComment(commentId: Int): Mono<Comment> {
        return commentRepository.findById(commentId)
            .switchIfEmpty(Mono.error(CommentNotFoundException("댓글을 찾을 수 없습니다. ID: $commentId")))
    }

    // 포스트로 모두 가져오기
    fun getCommentByPostId(postId: Int): Mono<List<CommentDTO>> {
        return commentRepository.findAllByPostIdAndUser(postId)
            .collectList()
            .map { commentsList ->
                val commentMap = mutableMapOf<Int, CommentDTO>()
                
                commentsList.forEach { commentsAll ->
                    val recomment = if (commentsAll.recomment_id != 0) {
                        ReCommentDTO(
                            id = commentsAll.recomment_id,
                            content = commentsAll.recomment_content,
                            createdAt = commentsAll.recomment_createdat,
                            user = UserDTO(
                                id = commentsAll.recomment_userid,
                                username = commentsAll.recomment_username,
                                email = commentsAll.recomment_email,
                                imageUrl = commentsAll.recomment_imageurl,
                                githubUrl = commentsAll.recomment_githuburl,
                                summary = commentsAll.recomment_summary
                            )
                        )
                    } else null
                    
                    val existingComment = commentMap[commentsAll.comment_id]
                    if (existingComment != null) {
                        recomment?.let { existingComment.recomments.add(it) }
                    } else {
                        val comment = CommentDTO(
                            id = commentsAll.comment_id,
                            content = commentsAll.comment_content,
                            createdAt = commentsAll.comment_createdat,
                            recomments = mutableListOf(),
                            user = UserDTO(
                                id = commentsAll.comment_userid,
                                username = commentsAll.comment_username,
                                hashedPassword = "",
                                email = commentsAll.comment_email,
                                imageUrl = commentsAll.comment_imageurl,
                                githubUrl = commentsAll.comment_githuburl,
                                summary = commentsAll.comment_summary
                            )
                        )
                        recomment?.let { comment.recomments.add(it) }
                        commentMap[commentsAll.comment_id] = comment
                    }
                }
                
                commentMap.values.toList()
            }
            .doOnSuccess { comments ->
                logger.debug("Found ${comments.size} comments for post $postId")
            }
    }
}