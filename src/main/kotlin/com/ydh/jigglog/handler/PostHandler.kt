package com.ydh.jigglog.handler

import com.ydh.jigglog.service.PostService
import com.ydh.jigglog.service.SecurityService
import com.ydh.jigglog.service.ValidationService
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
    @Autowired val validationService: ValidationService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PostHandler::class.java)
    }
    // 포스트 만들기
//    fun save(req: ServerRequest) = Mono.just(req)
//        // 병렬 실행 : 유저 로그인 확인, 폼 체크
//        .flatMap {
//            Mono.zip(
//                securityService.getLoggedInUser(it),
//                it.bodyToMono(Post::class.java).flatMap { post ->
//                    validationService.checkFormValid(post.toMono(),
//                            mapOf(
//                                "제목" to post.title,
//                                "내용" to post.content
//                            )) }.toMono()
//            )
//        // 포스트 저장
//        }.flatMap { it ->
//            val profile = it.t1
//            val post = it.t2
//            postService.createPost(profile, post)
//        }.flatMap {
//            ok().body(it.toMono())
//        }.onErrorResume(Exception::class.java) {
//            badRequest().body(
//                mapOf("message" to it.message).toMono()
//            )
//        }

    // 단일 포스트 가져오기
    fun get(req: ServerRequest) = Mono.just(req)
        // 포스트 가져오기
        .flatMap {
            postService.getPost(it.pathVariable("postId").toInt())
        }.flatMap {
            ok().body(
                it.toMono()
            )
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                Mono.just(it)
            )
        }

    // 포스트 업데이트
//    fun update(req: ServerRequest) = Mono.just(req)
//        // 병렬 실행 : 유저 로그인 확인, 폼 체크, 포스트 가져오기
//        .flatMap {
//            Mono.zip(
//                securityService.getLoggedInUser(it),
//                it.bodyToMono(Post::class.java).flatMap { post ->
//                    validationService.checkFormValid(post.toMono(),
//                        mapOf(
//                            "내용" to post.content,
//                        )) }.toMono(),
//                postService.getPost(req.pathVariable("postId").toInt())
//            )
//        // 포스트 작성자 체크
//        }.flatMap { it ->
//            val profile = it.t1
//            val postForm = it.t2
//            val post = it.t3
//            Mono.zip(
//                securityService.checkIsOwner<Post>(profile, post.profile!!, post.toMono()),
//                postForm.toMono()
//            )
//        // 업데이트
//        }.flatMap { it ->
//            val post = it.t1
//            val postForm = it.t2
//            logger.info(it.toString())
//            postService.updatePost(post, postForm).toMono()
//        }.flatMap {
//            ok().body(it.toMono())
//        }.onErrorResume(Exception::class.java) {
//            badRequest().body(
//                mapOf("message" to it.message).toMono()
//            )
//        }
//
//
//    // 포스트 삭제
//    fun delete(req: ServerRequest) = Mono.just(req)
//        // 병렬 실행 : 유저 로그인 확인, 포스트 가져오기
//        .flatMap {
//            Mono.zip(
//                securityService.getLoggedInUser(it),
//                postService.getPost(req.pathVariable("postId").toInt())
//            )
//            // 포스트 작성자 체크
//        }.flatMap { it ->
//            val profile = it.t1
//            val post = it.t2
//            Mono.zip(
//                securityService.checkIsOwner<Post>(profile, post.profile!!, post.toMono()),
//                req.pathVariable("postId").toInt().toMono()
//            )
//            // 삭제
//        }.flatMap { it ->
//            val postId = it.t2
//            postService.deletePost(postId).toMono()
//        }.flatMap {
//            ok().body(mapOf("message" to "포스트가 삭제되었습니다").toMono())
//        }.onErrorResume(Exception::class.java) {
//            badRequest().body(
//                mapOf("message" to it.message).toMono()
//            )
//        }
}
