package com.ydh.signight.handler

import com.ydh.signight.domain.dto.CommentFormDTO
import com.ydh.signight.service.*
import com.ydh.signight.util.ResponseHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class CommentHandler(
    @Autowired private val postService: PostService,
    @Autowired private val commentService: CommentService,
    @Autowired private val recommentService: ReCommentService,
    @Autowired private val securityService: SecurityService,
    @Autowired private val validationService: ValidationService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CommentHandler::class.java)
    }
    
    // 포스트 아이디로 모두 가져오기
    fun getByPostId(req: ServerRequest): Mono<ServerResponse> = 
        commentService.getCommentByPostId(req.pathVariable("postId").toInt())
            .flatMap { comments ->
                ResponseHelper.success(comments)
            }
            .onErrorResume { error ->
                logger.error("Failed to get comments for post", error)
                ResponseHelper.errorResponse(error)
            }
            
    // 만들기
    fun create(req: ServerRequest): Mono<ServerResponse> = 
        req.bodyToMono(CommentFormDTO::class.java)
            // 병렬 실행 : 폼 체크, 로그인 체크
            .flatMap { commentForm ->
                Mono.zip(
                    validationService.checkValidForm<CommentFormDTO>(
                        commentForm, 
                        mapOf("코멘트 내용" to commentForm.content)
                    ),
                    securityService.getLoggedInUser(req)
                )
            }
            // 생성
            .flatMap { tuple ->
                val commentForm = tuple.t1
                val user = tuple.t2
                val postId = req.pathVariable("postId").toInt()
                commentService.createComment(commentForm, user.id, postId)
                    .then(Mono.just(postId))
            }
            .flatMap { postId ->
                commentService.getCommentByPostId(postId)
            }
            .flatMap { comments ->
                ResponseHelper.success(comments)
            }
            .onErrorResume { error ->
                logger.error("Failed to create comment", error)
                ResponseHelper.errorResponse(error)
            }
            
    // 삭제
    fun delete(req: ServerRequest): Mono<ServerResponse> = 
        // 로그인 유저, 원본 댓글 가져오기
        Mono.zip(
            securityService.getLoggedInUser(req),
            commentService.getComment(req.pathVariable("commentId").toInt())
        )
        // 유저 체크
        .flatMap { tuple ->
            val user = tuple.t1
            val comment = tuple.t2
            securityService.checkIsOwner(user.id, comment.userId!!)
                .then(Mono.just(comment))
        }
        // 삭제
        .flatMap { comment ->
            commentService.deleteComment(comment.id)
                .then(Mono.just(comment.postId!!))
        }
        // 가져오기
        .flatMap { postId ->
            commentService.getCommentByPostId(postId)
        }
        .flatMap { comments ->
            ResponseHelper.success(comments)
        }
        .onErrorResume { error ->
            logger.error("Failed to delete comment", error)
            ResponseHelper.errorResponse(error)
        }
}



