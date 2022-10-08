package com.ydh.jigglog.handler

import com.ydh.jigglog.domain.Tag
import com.ydh.jigglog.service.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class TagHandler(
    @Autowired val tagService: TagService,
    @Autowired val postService: PostService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(TagHandler::class.java)
    }
    // 모두 가져오기
    fun getAll(req: ServerRequest) = Mono.just(req)
        .flatMap {
            tagService.getTagAllContainPost().collectList()
        }.flatMap {
            ok().body(
                Mono.just(it)
            )
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                Mono.just(it)
            )
        }

}



