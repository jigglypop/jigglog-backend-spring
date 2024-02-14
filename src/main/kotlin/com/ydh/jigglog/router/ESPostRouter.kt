package com.ydh.jigglog.router

import com.ydh.jigglog.domain.dto.handler.PostHandler
import org.springframework.context.annotation.Bean
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.router

@Component
@RequestMapping("/api/hello")
class ESPostRouter() {

    // 카테고리 목록 전부 가져오기
    @GetMapping("/all")
    fun getCategory(): ResponseEntity<String> {
        return ResponseEntity
            .status(200)
            .body("바디바디")
    }
}