package com.ydh.jigglog.router

import com.ydh.jigglog.handler.PostHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class PostRouter(private val handler: PostHandler) {

    @Bean
    fun postRouterFunction() = router {
        "/api/post".nest {
            POST("", handler::save)
            GET("/path", handler::path)
            GET("/{postId}", handler::get)
            PATCH("/{postId}", handler::update)
            DELETE("/{postId}", handler::delete)
        }
    }

}