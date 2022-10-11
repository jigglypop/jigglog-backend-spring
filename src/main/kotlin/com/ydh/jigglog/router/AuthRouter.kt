package com.ydh.jigglog.router

import com.ydh.jigglog.handler.AuthHandler
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
            GET("/check", handler::check)
        }
    }

}