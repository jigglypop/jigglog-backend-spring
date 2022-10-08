package com.ydh.jigglog.handler

import com.ydh.jigglog.domain.UserForm
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
class CategoryHandler(
    @Autowired val categoryService: CategoryService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CategoryHandler::class.java)
    }
    // 모두가져오기
    fun getAll(req: ServerRequest) = Mono.just(req)
        .flatMap {
            categoryService.getCategoryAll().collectList()
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



