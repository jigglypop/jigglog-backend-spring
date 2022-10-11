package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.*
import com.ydh.jigglog.domain.entity.Comment
import com.ydh.jigglog.domain.entity.ReComment
import com.ydh.jigglog.repository.CommentRepository
import com.ydh.jigglog.repository.ReCommentRepository
import com.ydh.jigglog.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
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
            for (everyComment in it) {
                val comment = CommentDTO(
                    id = everyComment.id,
                    content = everyComment.content,
                    createdAt = everyComment.createdat,
                    recomments = mutableListOf<ReCommentDTO>(),
                    user = UserDTO(
                        id = everyComment.userid,
                        username = everyComment.username,
                        hashedPassword = "",
                        email = everyComment.email,
                        imageUrl = everyComment.imageurl,
                        githubUrl = everyComment.githuburl,
                        summary = everyComment.summary,
                    )
                )
                comment_idx[everyComment.id] = comment
            }
            Mono.zip(
                comment_idx.toMono(),
                recommentRepository.findEveryRecomments(postId).collectList().toMono()
            )
        }.flatMap {
            val comment_idx = it.t1
            val recommentDTOs = it.t2
            for (everyReComment in recommentDTOs) {
                val recomment = ReCommentDTO(
                    id = everyReComment.id,
                    content = everyReComment.content,
                    createdAt = everyReComment.createdat,
                    user = UserDTO(
                        id = everyReComment.userid,
                        username = everyReComment.username,
                        hashedPassword = "",
                        email = everyReComment.email,
                        imageUrl = everyReComment.imageurl,
                        githubUrl = everyReComment.githuburl,
                        summary = everyReComment.summary,
                    )
                )
                comment_idx[everyReComment.commentid]!!.recomments!!.add(recomment)
            }
            val results = mutableListOf<CommentDTO>()
            for (i in comment_idx.keys) {
                results.add(comment_idx[i]!!)
            }
            results.toMono()
        }
    }

}