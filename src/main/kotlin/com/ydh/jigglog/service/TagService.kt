package com.ydh.jigglog.service

import com.ydh.jigglog.domain.Post
import com.ydh.jigglog.domain.Tag
import com.ydh.jigglog.handler.AuthHandler
import com.ydh.jigglog.repository.TagRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono

@Controller
class TagService (
   private val tagRepository: TagRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthHandler::class.java)
    }
    // 카테고리 모두 가져오기
    fun getTagAll(): Flux<Tag> {
        return tagRepository.findAll()
    }

    // 태그의 포스트 가져오기
    fun getTagToPostAll(tagId: Int): Flux<Post> {
        return tagRepository.findPostsByTagId(tagId)
    }

    // 태그의 포스트 (post 가 있는) 가져오기
    fun getTagAllContainPost(): Flux<Tag> {
        return tagRepository.findTagsAllContainPost()
    }
}