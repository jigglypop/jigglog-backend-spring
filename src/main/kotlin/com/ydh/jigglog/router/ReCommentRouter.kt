package com.ydh.jigglog.router

import com.ydh.jigglog.handler.ReCommentHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class ReCommentRouter(private val handler: ReCommentHandler) {
    @Bean
    fun recommentRouterFunction() = router {
        "/api/recomment".nest {
            POST("/{commentId}", handler::create)
            DELETE("/{recommentId}", handler::delete)
        }
    }
}