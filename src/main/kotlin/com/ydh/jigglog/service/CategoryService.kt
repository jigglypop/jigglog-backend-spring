package com.ydh.jigglog.service

import com.ydh.jigglog.domain.Category
import com.ydh.jigglog.domain.User
import com.ydh.jigglog.domain.UserForm
import com.ydh.jigglog.handler.AuthHandler
import com.ydh.jigglog.repository.CategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Controller
class CategoryService (
   private val categoryRepository: CategoryRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthHandler::class.java)
    }
    // 카테고리 모두 가져오기
    fun getCategoryAll(): Flux<Category> {
        return categoryRepository.findAll()
    }
}