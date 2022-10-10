package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.PostToTag
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PostToTagRepository: ReactiveCrudRepository<PostToTag, Int> {
}