package com.ydh.jigglog.handler

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
class ResumeHandler(
    @Autowired val postService: PostService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ResumeHandler::class.java)
    }
    // 이력서 가져오기
    fun get(req: ServerRequest) = Mono.just(req)
        // 포스트 가져오기
        .flatMap {
            postService.getPost(1)
        }.flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
}
