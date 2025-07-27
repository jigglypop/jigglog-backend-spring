package com.ydh.jigglog.handler

import com.ydh.jigglog.service.*
import com.ydh.jigglog.util.ResponseHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class CategoryHandler(
    @Autowired val categoryService: CategoryService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CategoryHandler::class.java)
    }
    
    // 모두가져오기
    fun getAll(req: ServerRequest): Mono<ServerResponse> = 
        categoryService.getCategoryAll()
            .collectList()
            .flatMap { categories ->
                ResponseHelper.success(categories)
            }
            .onErrorResume { error ->
                logger.error("Failed to get all categories", error)
                ResponseHelper.errorResponse(error)
            }
            
    fun getAllAndCache(req: ServerRequest): Mono<ServerResponse> = 
        categoryService.getAllAndCache()
            .flatMap { categories ->
                ResponseHelper.success(categories)
            }
            .onErrorResume { error ->
                logger.error("Failed to get categories from cache", error)
                ResponseHelper.errorResponse(error)
            }
            
    // 카테고리 아이디로 포스트 모두 가져오기
    fun getAllPostByCategoryId(req: ServerRequest): Mono<ServerResponse> = 
        Mono.fromSupplier {
            val page = req.queryParam("page").map { it.toInt() }.orElse(1)
            val limit = req.queryParam("limit").map { it.toInt() }.orElse(8)
            val offset = (page - 1) * limit
            Triple(req.pathVariable("categoryId").toInt(), offset, limit)
        }
        .flatMap { triple ->
            categoryService.getAllPostByCategoryId(triple.first, triple.second, triple.third)
        }
        .flatMap { posts ->
            ResponseHelper.success(posts)
        }
        .onErrorResume { error ->
            logger.error("Failed to get posts by category", error)
            ResponseHelper.errorResponse(error)
        }
}



