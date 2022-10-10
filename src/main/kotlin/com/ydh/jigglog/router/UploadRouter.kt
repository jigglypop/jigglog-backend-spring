package com.ydh.jigglog.router

import com.ydh.jigglog.handler.UploadHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class UploadRouter(private val handler: UploadHandler) {

    @Bean
    fun uploadRouterFunction() = router {
        "/api".nest {
            "/upload".nest {
                POST("", handler::upload)
            }
        }
    }

}