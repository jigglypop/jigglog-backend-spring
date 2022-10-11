package com.ydh.jigglog.service

import com.ydh.jigglog.domain.entity.Tag
import com.ydh.jigglog.repository.PostToTagRepository
import com.ydh.jigglog.repository.TagRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Controller
class TagService (
    @Autowired private val tagRepository: TagRepository,
    @Autowired private val postToTagRepository: PostToTagRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(TagService::class.java)
    }
    // 태그 만들기
    fun createTagAll(titles: List<String>): Mono<List<Tag>> {
        var tags = mutableListOf<Tag>()
        for (title in titles) {
            tags.add(Tag(title = title))
        }
        return tagRepository.saveAll(tags).collectList().toMono()
    }
    // 태그의 포스트 중 없는 거 가져오기
    fun getTagNotExist(parsedTags: Flux<String>, tags: List<String>): Flux<String> {
        return parsedTags.filter {
            title -> title !in tags && title != ""
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
        return Mono.just(tagsString)
            // 모든 태그 가져오기
            .flatMap { tagsString ->
                Mono.zip(
                    getTagAllContainPost().collectList().toMono(),
                    tagsString.toMono())
            }
            // 1) 원문 태그 파싱
            // 2) 없는 태그 모으기
            .flatMap {
                val tagsOrg = it.t1
                val tagsString = it.t2
                val parsedTags = tagsString.split("#")
                val parsedTagFlux = parsedTags.toFlux()
                val tags = mutableListOf<String>()
                for (tag in tagsOrg) {
                    tags.add(tag.title!!)
                }
                Mono.zip(
                    parsedTags.filter { tag -> tag != "" }.toMono(),
                    getTagNotExist(parsedTagFlux, tags).collectList().toMono()
                )
            }
            // 태그 모두 만들기
            .flatMap {
                val parsedTags = it.t1
                val tagsNotExist = it.t2
                Mono.zip(
                    parsedTags.toMono(),
                    createTagAll(tagsNotExist).toMono()
                )
            }
            // 해당 제목 태그 모두 리턴
            .flatMap {
                val parsedTags = it.t1
                getTagAllByTitle(parsedTags).collectList().toMono()
            }
    }
    // 태그 아이디로 조인 삭제
    fun deleteTagsByTagID(tagId: Int): Mono<Void> {
        return Mono.just(tagId)
            .flatMap {
                postToTagRepository.deleteByTagId(it).toMono()
            }
    }
    // 포스트 아이디로 조인 삭제
    fun deleteTagsByPostID(postId: Int): Mono<Void> {
        return Mono.just(postId)
            .flatMap {
                postToTagRepository.deleteByPostId(it).toMono()
            }
    }
}