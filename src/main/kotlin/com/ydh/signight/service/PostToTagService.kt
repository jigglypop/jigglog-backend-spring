package com.ydh.signight.service

import com.ydh.signight.domain.entity.PostToTag
import com.ydh.signight.repository.PostToTagRepository
import com.ydh.signight.repository.TagRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class PostToTagService (
    @Autowired private val tagRepository: TagRepository,
    @Autowired private val postToTagRepository: PostToTagRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PostToTagService::class.java)
    }
    // 연결하기
    fun bindPostToTag(postId: Int, tagId: Int): Mono<PostToTag> {
        return postToTagRepository.save(PostToTag(postId = postId, tagId = tagId))
    }
}