package com.ydh.jigglog.router

import com.ydh.jigglog.handler.ResumeHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class ResumeRouter(private val handler: ResumeHandler) {

    @Bean
    fun resumeRouterFunction() = router {
        "/api/rresume".nest {
            GET("", handler::get)
        }
    }

}