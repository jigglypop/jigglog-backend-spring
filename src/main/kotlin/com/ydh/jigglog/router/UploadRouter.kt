package com.ydh.jigglog.router

import com.ydh.jigglog.handler.UploadHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class UploadRouter(private val uploadHandler: UploadHandler) {

    @Bean
    fun uploadRoutes() = router {
        POST("/api/upload", uploadHandler::uploadFile)
        DELETE("/api/upload/{fileName}", uploadHandler::deleteFile)
    }
}