package com.ydh.jigglog.handler

import com.ydh.jigglog.domain.dto.PostFormDTO
import com.ydh.jigglog.domain.dto.UpdateFormDTO
import com.ydh.jigglog.service.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.body
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
    fun save(req: ServerRequest) = req
        .bodyToMono(PostFormDTO::class.java)
        // 병렬 실행 : 폼 체크, 관리자 체크
        .flatMap {
            Mono.zip(
                validationService.checkValidForm<PostFormDTO>(
                    it, mapOf(
                        "포스트 제목" to it.title,
                        "포스트 요약" to it.summary,
                        "포스트 내용" to it.content,
                        "타이틀 이미지" to it.images,
                        "카테고리 제목" to it.category_title
                    )
                ).toMono(),
                securityService.getLoggedInUser(req).toMono(),
            )
        // 유저 관리자 검사
        }.flatMap {
            val postForm = it.t1
            val user = it.t2
            Mono.zip(
                securityService.isOwner(user),
                postForm.toMono(),
                user.toMono()
            )
        // 태그 생성하기, 카테고리 생성하기
        }.flatMap {
            val postForm = it.t2
            val user = it.t3
            Mono.zip(
                tagService.createTagParseAndMakeAll(postForm.tags!!).toMono(),
                categoryService.createCategoryIfNot(postForm.category_title!!),
                postForm.toMono(),
                user.toMono()
            )
        // 병렬 실행 : 포스트 생성하기
        }.flatMap {
            val tags = it.t1
            val category = it.t2
            val postForm = it.t3
            val user = it.t4
            postService.createPost(user, postForm, category, tags)
        }.flatMap {
            Mono.zip(
                it.toMono(),
                categoryService.resetCategoryCash()
            )

        }.flatMap {
            ok().body(it.t1.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }

    // 단일 포스트 가져오기
    fun get(req: ServerRequest) = Mono.just(req)
        // 포스트 가져오기
        .flatMap {
            postService.getPost(it.pathVariable("postId").toInt())
        }.flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
    // 포스트 패스 가져오기
    fun path(req: ServerRequest) = Mono.just(req)
        // 포스트 가져오기
        .flatMap {
            postService.getPostPath()
        }.flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
    // 포스트 업데이트
    fun update(req: ServerRequest) = req
        .bodyToMono(UpdateFormDTO::class.java)
        // 병렬 실행 : 폼 체크, 관리자 체크
        .flatMap { postForm->
            Mono.zip(
                postForm.toMono(),
                securityService.getLoggedInUser(req).toMono(),
            )
        // 유저 관리자 검사, 포스트 원본 가져오기
        }.flatMap {
            val postForm = it.t1
            val user = it.t2
            Mono.zip(
                postForm.toMono(),
                postService.getOnlyPost(req.pathVariable("postId").toInt()).toMono(),
                securityService.isOwner(user),
            )
        // 포스트 업데이트하기
        }.flatMap {
            val postForm = it.t1
            val post = it.t2
            postService.updatePost(post, postForm).toMono()
        }.flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }

    // 포스트 삭제
    fun delete(req: ServerRequest) = Mono.just(req)
        // 관리자 체크
        .flatMap {
            securityService.getLoggedInUser(req).toMono()
        // 유저 관리자 검사, 포스트 원본 가져오기
        }.flatMap { user ->
            securityService.isOwner(user)
        // 포스트 삭제하기
        }.flatMap {
            postService.deltePost(req.pathVariable("postId").toInt()).toMono()
        }.flatMap {
            categoryService.resetCategoryCash()
            ok().body(mapOf("message" to "포스트 삭제가 완료되었습니다.").toMono()).toMono()
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
}
