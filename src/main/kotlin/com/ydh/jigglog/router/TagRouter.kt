package com.ydh.jigglog.router

import com.ydh.jigglog.handler.TagHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class TagRouter(private val handler: TagHandler) {

    @Bean
    fun tagRouterFunction() = router {
        "/api".nest {
            "/tag".nest {
                GET("", handler::getAll)
//                GET("/{categoryId}", handler::check)
            }
        }
    }

}