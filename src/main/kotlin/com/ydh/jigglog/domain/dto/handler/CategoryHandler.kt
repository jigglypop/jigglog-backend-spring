package com.ydh.jigglog.domain.dto.handler

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

    // 카테고리 아이디로 포스트 모두 가져오기
    fun getAllPostByCategoryId(req: ServerRequest) = Mono.just(req)
        .flatMap {
            val page = req.queryParams()?.get("page")?.get(0)?.toInt()
            val limit = req.queryParams()?.get("limit")?.get(0)?.toInt() ?: 8
            val offset = ((page ?: 1) - 1) * 8
            categoryService.getAllPostByCategoryId(req.pathVariable("categoryId").toInt(), offset, limit)
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



