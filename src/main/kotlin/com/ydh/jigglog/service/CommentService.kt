package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.CommentDTO
import com.ydh.jigglog.domain.dto.CommentFormDTO
import com.ydh.jigglog.domain.dto.ReCommentDTO
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

    // 포스트 아이디로 모든 코멘트 가져오기
    fun getCommentAll(postId: Int): Mono<List<Comment>> {
        return commentRepository.findAllByPostId(postId).collectList().toMono()
    }
    // 포스트로 모두 가져오기
    fun getCommentByPostId(comments: List<Comment>, postId: Int): Mono<List<CommentDTO>> {
        return Mono.just(comments)
            .flatMap {
                val comments = it
                val user_comment = mutableMapOf<Int, MutableList<List<Int>>>()
                val user_idx = mutableSetOf<Int>()
                val comment_id = mutableListOf<Int>()
                val result = mutableListOf<CommentDTO>()
                for (i in comments.indices) {
                    var comment = comments[i]
                    result.add(CommentDTO(
                        id = comment.id,
                        content = comment.content
                    ))
                    // comment의 경우 (1)
                    val temp = mutableListOf<Int>()
                    temp.add(1)
                    temp.add(comment.userId!!)
                    if (comment.userId in user_comment) {
                        user_comment[i]!!.add(temp)
                    } else {
                        user_comment[i] = mutableListOf(temp)
                    }
                    user_idx.add(comment.userId!!)
                    comment_id.add(comment.id!!)
                }
                Mono.zip(
                    comments.toMono(),
                    user_comment.toMono(),
                    user_idx.toMono(),
                    recommentRepository.findAllByCommentIdIn(comment_id).collectList().toMono(),
                    result.toMono()
                )
            }.flatMap {
                val comments = it.t1
                val user_comment = it.t2
                val user_idx = it.t3
                val recomments = it.t4
                val result = it.t5
                val comment_recomment = mutableMapOf<Int, MutableList<Int>>()
                for (i in recomments.indices) {
                    var recomment = recomments[i]
                    result.add(
                        ReCommentDTO(
                        id = recomment.id,
                        content = recomment.content))
                }
            }
    }
}