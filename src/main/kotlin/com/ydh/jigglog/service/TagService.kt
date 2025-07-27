package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.PostInCategoryDTO
import com.ydh.jigglog.domain.dto.UserInPostCategoryDTO
import com.ydh.jigglog.domain.entity.Tag
import com.ydh.jigglog.repository.PostRepository
import com.ydh.jigglog.repository.PostToTagRepository
import com.ydh.jigglog.repository.TagRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Service
class TagService (
    @Autowired private val tagRepository: TagRepository,
    @Autowired private val postRepository: PostRepository,
    @Autowired private val postToTagRepository: PostToTagRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(TagService::class.java)
    }
    
    // 태그 만들기
    fun createTagAll(titles: List<String>): Mono<List<Tag>> {
        val tags = titles.map { title -> Tag(title = title) }
        return tagRepository.saveAll(tags)
            .collectList()
            .doOnSuccess { 
                logger.info("Created ${it.size} tags")
            }
    }
    
    // 태그의 포스트 중 없는 거 가져오기
    fun getTagNotExist(parsedTags: Flux<String>, existingTags: List<String>): Flux<String> {
        return parsedTags.filter { title -> 
            title !in existingTags && title.isNotBlank()
        }
    }
    
    // 태그의 포스트 (post 가 있는) 가져오기
    fun getTagAllContainPost(): Flux<Tag> {
        return tagRepository.findTagsAllContainPost()
    }
    
    // 태그 제목으로 모두 가져오기
    fun getTagAllByTitle(titles: List<String>): Flux<Tag> {
        return tagRepository.findAllByTitleIn(titles)
    }
    
    // 태그 생성
    fun createTagParseAndMakeAll(tagsString: String): Mono<MutableList<Tag>> {
        return getTagAllContainPost()
            .collectList()
            .flatMap { existingTags ->
                val parsedTags = tagsString.split("#").filter { it.isNotBlank() }
                val existingTagTitles = existingTags.map { it.title!! }
                val newTagTitles = parsedTags.filter { it !in existingTagTitles }
                
                if (newTagTitles.isNotEmpty()) {
                    createTagAll(newTagTitles)
                        .then(getTagAllByTitle(parsedTags).collectList())
                } else {
                    getTagAllByTitle(parsedTags).collectList()
                }
            }
            .doOnSuccess { tags ->
                logger.debug("Processed tags: ${tags.map { it.title }}")
            }
    }
    
    // 태그 아이디로 조인 삭제
    fun deleteTagsByTagID(tagId: Int): Mono<Void> {
        return postToTagRepository.deleteByTagId(tagId)
            .doOnSuccess { 
                logger.info("Deleted tag associations for tag: $tagId")
            }
    }
    
    // 포스트 아이디로 조인 삭제
    fun deleteTagsByPostID(postId: Int): Mono<Void> {
        return postToTagRepository.deleteByPostId(postId)
            .doOnSuccess { 
                logger.info("Deleted tag associations for post: $postId")
            }
    }

    // 태그 아이디로 포스트 가져오기
    fun getAllPostByTagId(tagId: Int, offset: Int, limit: Int? = 8): Mono<List<PostInCategoryDTO>> {
        return postRepository.findAllByTagId(tagId, offset, limit)
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
                logger.debug("Found ${posts.size} posts for tag $tagId")
            }
    }
}