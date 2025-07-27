package com.ydh.signight.router

import com.ydh.signight.handler.AuthHandler
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class AuthRouter(private val handler: AuthHandler) {
    @Bean
    fun authRouterFunction() = router {
        accept(MediaType.APPLICATION_JSON)
            .nest {
                "/api/auth".nest {
                    GET("/test", handler::test)
                    POST("/register", handler::register)
                    POST("/login", handler::login)
                    POST("/comment", handler::comment)
                    GET("/check", handler::check)
                }
        }
    }

}