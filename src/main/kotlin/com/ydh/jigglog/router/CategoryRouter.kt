package com.ydh.jigglog.router

import com.ydh.jigglog.handler.CategoryHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class CategoryRouter(private val handler: CategoryHandler) {
    @Bean
    fun categoryRouterFunction() = router {
        "/api/rcategory".nest {
            GET("", handler::getAll)
//                GET("/{categoryId}", handler::check)
        }
    }

}