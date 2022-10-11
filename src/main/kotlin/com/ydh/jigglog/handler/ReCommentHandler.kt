package com.ydh.jigglog.handler

import com.ydh.jigglog.domain.dto.ReCommentFormDTO
import com.ydh.jigglog.domain.dto.PostFormDTO
import com.ydh.jigglog.service.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono

import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class ReCommentHandler(
    @Autowired private val postService: PostService,
    @Autowired private val commentService: CommentService,
    @Autowired private val recommentService: ReCommentService,
    @Autowired private val securityService: SecurityService,
    @Autowired private val validationService: ValidationService,

    ) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReCommentHandler::class.java)
    }
    // 만들기
    fun create(req: ServerRequest) = req.bodyToMono(ReCommentFormDTO::class.java)
        // 병렬 실행 : 폼 체크, 로그인 체크
        .flatMap {
            Mono.zip(
                validationService.checkValidForm<ReCommentFormDTO>(
                    it, mapOf(
                        "대댓글 내용" to it.content,
                    )
                ).toMono(),
                securityService.getLoggedInUser(req).toMono(),
            )
        // 생성
        }.flatMap {
            val recommentForm = it.t1
            val user = it.t2
            val commentId = req.pathVariable("commentId").toInt()
            recommentService.createReComment(recommentForm, user.id, commentId)
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
                recommentService.getReComment(req.pathVariable("recommentId").toInt()).toMono()
            )
        // 유저 체크
        }.flatMap {
            val user = it.t1
            val recomment = it.t2
            Mono.zip(
                securityService.checkIsOwner(user.id, recomment.userId!!).toMono(),
                recomment.toMono()
            )
        // 삭제
        }.flatMap {
            val recomment = it.t2
            recommentService.deleteReComment(recomment.id).toMono()
        }.flatMap {
            ok().body(mapOf("message" to "대댓글 삭제가 완료되었습니다.").toMono()).toMono()
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
}



