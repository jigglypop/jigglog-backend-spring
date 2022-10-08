package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.Category
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: ReactiveCrudRepository<Category, Int> {
}