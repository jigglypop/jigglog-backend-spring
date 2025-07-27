package com.ydh.signight.handler

import com.ydh.signight.domain.dto.PostFormDTO
import com.ydh.signight.domain.dto.UpdateFormDTO
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
class PostHandler(
    @Autowired val postService: PostService,
    @Autowired val securityService: SecurityService,
    @Autowired val validationService: ValidationService,
    @Autowired val categoryService: CategoryService,
    @Autowired val tagService: TagService,
    @Autowired val postToTagService: PostToTagService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PostHandler::class.java)
    }
    
    // 포스트 만들기
    fun save(req: ServerRequest): Mono<ServerResponse> = 
        req.bodyToMono(PostFormDTO::class.java)
            // 병렬 실행 : 폼 체크, 관리자 체크
            .flatMap { postForm ->
                Mono.zip(
                    validationService.checkValidForm<PostFormDTO>(
                        postForm, mapOf(
                            "포스트 제목" to postForm.title,
                            "포스트 요약" to postForm.summary,
                            "포스트 내용" to postForm.content,
                            "타이틀 이미지" to postForm.images,
                            "카테고리 제목" to postForm.category_title
                        )
                    ),
                    securityService.getLoggedInUser(req)
                )
            }
            // 유저 관리자 검사
            .flatMap { tuple ->
                val postForm = tuple.t1
                val user = tuple.t2
                securityService.isOwner(user)
                    .then(Mono.just(postForm to user))
            }
            // 태그 생성하기, 카테고리 생성하기
            .flatMap { pair ->
                val postForm = pair.first
                val user = pair.second
                Mono.zip(
                    tagService.createTagParseAndMakeAll(postForm.tags!!),
                    categoryService.createCategoryIfNot(postForm.category_title!!),
                    Mono.just(postForm),
                    Mono.just(user)
                )
            }
            // 병렬 실행 : 포스트 생성하기
            .flatMap { tuple ->
                val tags = tuple.t1
                val category = tuple.t2
                val postForm = tuple.t3
                val user = tuple.t4
                postService.createPost(user, postForm, category, tags)
            }
            .flatMap { post ->
                categoryService.resetCategoryCash()
                    .then(Mono.just(post))
            }
            .flatMap { post ->
                ResponseHelper.success(post)
            }
            .onErrorResume { error ->
                logger.error("Failed to create post", error)
                ResponseHelper.errorResponse(error)
            }

    // 단일 포스트 가져오기
    fun get(req: ServerRequest): Mono<ServerResponse> = 
        Mono.just(req.pathVariable("postId").toInt())
            // 포스트 가져오기
            .flatMap { postId ->
                postService.getPost(postId)
            }
            .flatMap { post ->
                ResponseHelper.success(post!!)
            }
            .onErrorResume { error ->
                logger.error("Failed to get post", error)
                ResponseHelper.errorResponse(error)
            }
            
    // 포스트 패스 가져오기
    fun path(req: ServerRequest): Mono<ServerResponse> = 
        postService.getPostPath()
            .flatMap { paths ->
                ResponseHelper.success(paths)
            }
            .onErrorResume { error ->
                logger.error("Failed to get post paths", error)
                ResponseHelper.errorResponse(error)
            }
            
    // 포스트 업데이트
    fun update(req: ServerRequest): Mono<ServerResponse> = 
        req.bodyToMono(UpdateFormDTO::class.java)
            // 병렬 실행 : 폼 체크, 관리자 체크
            .flatMap { postForm ->
                Mono.zip(
                    Mono.just(postForm),
                    securityService.getLoggedInUser(req)
                )
            }
            // 유저 관리자 검사, 포스트 원본 가져오기
            .flatMap { tuple ->
                val postForm = tuple.t1
                val user = tuple.t2
                Mono.zip(
                    Mono.just(postForm),
                    postService.getOnlyPost(req.pathVariable("postId").toInt()),
                    securityService.isOwner(user)
                )
            }
            // 포스트 업데이트하기
            .flatMap { tuple ->
                val postForm = tuple.t1
                val post = tuple.t2
                postService.updatePost(post, postForm)
            }
            .flatMap { updatedPost ->
                ResponseHelper.success(updatedPost!!)
            }
            .onErrorResume { error ->
                logger.error("Failed to update post", error)
                ResponseHelper.errorResponse(error)
            }

    // 포스트 삭제
    fun delete(req: ServerRequest): Mono<ServerResponse> = 
        securityService.getLoggedInUser(req)
            // 유저 관리자 검사
            .flatMap { user ->
                securityService.isOwner(user)
            }
            // 포스트 삭제하기
            .flatMap {
                postService.deletePost(req.pathVariable("postId").toInt())
            }
            .flatMap {
                categoryService.resetCategoryCash()
            }
            .flatMap {
                ResponseHelper.success(mapOf("message" to "포스트 삭제가 완료되었습니다."))
            }
            .onErrorResume { error ->
                logger.error("Failed to delete post", error)
                ResponseHelper.errorResponse(error)
            }
}
