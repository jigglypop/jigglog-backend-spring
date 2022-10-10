package com.ydh.jigglog.service

import com.ydh.jigglog.domain.entity.Category
import com.ydh.jigglog.repository.CategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class CategoryService (
    @Autowired private val categoryRepository: CategoryRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CategoryService::class.java)
    }
    // 카테고리 모두 가져오기
    fun getCategoryAll(): Flux<Category> {
        return categoryRepository.findAll()
    }
    // 카테고리 확인하고 없으면 생성
    fun createCategoryIfNot(title: String): Mono<Category> {
        return categoryRepository.existsByTitle(title).flatMap {
            if (it) {
                categoryRepository.findByTitle(title)
            } else {
                categoryRepository.save(Category(title = title))
            }
        }
    }
}