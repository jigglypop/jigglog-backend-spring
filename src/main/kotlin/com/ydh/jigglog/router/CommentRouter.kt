package com.ydh.jigglog.router

import com.ydh.jigglog.domain.dto.handler.CommentHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class CommentRouter(private val handler: CommentHandler) {
    @Bean
    fun commentRouterFunction() = router {
        "/api/comment".nest {
            GET("/{postId}", handler::getByPostId)
            POST("/{postId}", handler::create)
            DELETE("/{commentId}", handler::delete)
        }
    }
}