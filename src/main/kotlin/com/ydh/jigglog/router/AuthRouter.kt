package com.ydh.jigglog.router

import com.ydh.jigglog.domain.dto.handler.AuthHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class AuthRouter(private val handler: AuthHandler) {
    @Bean
    fun authRouterFunction() = router {
        "/api/auth".nest {
            GET("/test", handler::test)
            POST("/register", handler::register)
            POST("/login", handler::login)
            POST("/comment", handler::comment)
            GET("/check", handler::check)
        }
    }

}