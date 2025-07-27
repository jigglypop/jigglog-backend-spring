package com.ydh.signight.service

import com.ydh.signight.domain.dto.CategoryDTO
import com.ydh.signight.domain.dto.CategoryListDTO
import com.ydh.signight.domain.dto.PostInCategoryDTO
import com.ydh.signight.domain.dto.UserInPostCategoryDTO
import com.ydh.signight.domain.entity.Category
import com.ydh.signight.repository.CategoryCacheRepository
import com.ydh.signight.repository.CategoryRepository
import com.ydh.signight.repository.PostRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class CategoryService (
    @Autowired private val categoryRepository: CategoryRepository,
    @Autowired private val categoryCacheRepository: CategoryCacheRepository,
    @Autowired private val postRepository: PostRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CategoryService::class.java)
    }
    
    // 카테고리 모두 가져오기
    fun getCategoryAll(): Flux<CategoryDTO> {
        return categoryRepository.findAllAndCount()
    }

    // 카테고리 아이디로 포스트 가져오기
    fun getAllPostByCategoryId(categoryId: Int, offset: Int, limit: Int? = 8): Mono<List<PostInCategoryDTO>> {
        return postRepository.findAllByCategoryId(categoryId, offset, limit)
            .map { post ->
                PostInCategoryDTO(
                    id = post.id,
                    summary = post.summary,
                    title = post.title,
                    createdAt = post.createdat,
                    postcount = post.postcount,
                    viewcount = post.viewcount,
                    commentcount = post.commentcount,
                    last = post.last,
                    images = post.images,
                    user = UserInPostCategoryDTO(
                        id = post.userid,
                        username = post.username,
                        imageUrl = post.imageurl
                    )
                )
            }
            .collectList()
            .doOnSuccess { posts ->
                logger.debug("Found ${posts.size} posts for category $categoryId")
            }
    }

    // 카테고리 확인하고 없으면 생성
    fun createCategoryIfNot(title: String): Mono<Category> {
        return categoryRepository.existsByTitle(title)
            .flatMap { exists ->
                if (exists) {
                    categoryRepository.findByTitle(title)
                } else {
                    categoryRepository.save(Category(title = title))
                        .doOnSuccess { logger.info("Created new category: $title") }
                }
            }
    }

    // 카테고리 캐시 확인하고 없으면 생성
    fun getAllAndCache(): Mono<MutableList<CategoryDTO>> {
        return categoryCacheRepository.findAllAndCaching()
            .switchIfEmpty(
                categoryRepository
                    .findAllAndCount()
                    .collectList()
                    .flatMap { categories ->
                        categoryCacheRepository.setCategoriesAllAndCaching(categories)
                    }
            )
            .map { categoryListDTO ->
                categoryListDTO.categories
            }
    }

    // 카테고리 캐시 리셋
    fun resetCategoryCash(): Mono<CategoryListDTO> {
        return categoryRepository
            .findAllAndCount()
            .collectList()
            .flatMap { categories ->
                categoryCacheRepository.setCategoriesAllAndCaching(categories)
            }
            .doOnSuccess { 
                logger.info("Category cache has been reset")
            }
    }
}