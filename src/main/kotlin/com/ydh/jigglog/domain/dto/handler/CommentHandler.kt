package com.ydh.jigglog.domain.dto.handler

import com.ydh.jigglog.domain.dto.CommentFormDTO
import com.ydh.jigglog.service.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body

import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class CommentHandler(
    @Autowired private val postService: PostService,
    @Autowired private val commentService: CommentService,
    @Autowired private val recommentService: ReCommentService,
    @Autowired private val securityService: SecurityService,
    @Autowired private val validationService: ValidationService,

    ) {
    companion object {
        private val logger = LoggerFactory.getLogger(CommentHandler::class.java)
    }
    // 포스트 아이디로 모두 가져오기
    fun getByPostId(req: ServerRequest) = Mono.just(req)
        // 포스트의 모든 댓글 가져오기
        .flatMap {
            commentService.getCommentByPostId(req.pathVariable("postId").toInt()).toMono()
        }.flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
    // 만들기
    fun create(req: ServerRequest) = req.bodyToMono(CommentFormDTO::class.java)
        // 병렬 실행 : 폼 체크, 로그인 체크
        .flatMap {
            Mono.zip(
                validationService.checkValidForm<CommentFormDTO>(
                    it, mapOf(
                        "코멘트 내용" to it.content,
                    )
                ).toMono(),
                securityService.getLoggedInUser(req).toMono(),
            )
        // 생성
        }.flatMap {
            val commentForm = it.t1
            val user = it.t2
            val postId = req.pathVariable("postId").toInt()
            Mono.zip(
                commentService.createComment(commentForm, user.id, postId).toMono(),
                postId.toMono()
            )
        }.flatMap {
            commentService.getCommentByPostId(it.t2)
        }.flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
    // 삭제
    fun delete(req: ServerRequest) = Mono.just(req)
        // 로그인 유저, 원본 댓글 가져오기
        .flatMap {
            Mono.zip(
                securityService.getLoggedInUser(req).toMono(),
                commentService.getComment(req.pathVariable("commentId").toInt()).toMono()
            )
        // 유저 체크
        }.flatMap {
            val user = it.t1
            val comment = it.t2
            val postId = comment.postId
            Mono.zip(
                securityService.checkIsOwner(user.id, comment.userId!!).toMono(),
                comment.toMono(),
                postId.toMono()
            )
        // 삭제
        }.flatMap {
            val comment = it.t2
            val postId = it.t3
            Mono.zip(
                commentService.deleteComment(comment.id).toMono(),
                postId.toMono()
            )
        // 가져오기
        }.flatMap {
            val postId = it.t2
            commentService.getCommentByPostId(postId).toMono()
        }.flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
}



