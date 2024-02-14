package com.ydh.jigglog.domain.dto.handler

import com.ydh.jigglog.domain.entity.Tag
import com.ydh.jigglog.service.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body

import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class TagHandler(
    @Autowired private val tagService: TagService,
    ) {
    companion object {
        private val logger = LoggerFactory.getLogger(TagHandler::class.java)
    }
    // 모두 가져오기
    fun getAll(req: ServerRequest) = Mono.just(req)
        .flatMap {
            tagService.getTagAllContainPost().collectList()
        }.flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
    // 만들기
    fun create(req: ServerRequest) = req.bodyToMono(Tag::class.java)
        .flatMap {
            tagService.createTagParseAndMakeAll(it.title!!).toMono()
        }.flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }

    // 포스트 아이디로 조인 삭제하기
    fun deleteJoinByPostId(req: ServerRequest) = Mono
        .just(req.pathVariable("postId"))
        .flatMap {
            tagService.deleteTagsByPostID(it.toInt()).toMono()
        }.flatMap {
            ok().body(mapOf("message" to "포스트: $it 삭제가 완료되었습니다").toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }

    // 태그 아이디로 조인 삭제하기
    fun deleteJoinByTagID(req: ServerRequest) = Mono
        .just(req.pathVariable("postId"))
        .flatMap {
            tagService.deleteTagsByTagID(it.toInt()).toMono()
        }.flatMap {
            ok().body(mapOf("message" to "포스트: $it 삭제가 완료되었습니다").toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }

    // 태그 아이디로 포스트 모두 가져오기
    fun getAllPostByTagId(req: ServerRequest) = Mono.just(req)
        .flatMap {
            val page = req.queryParams()?.get("page")?.get(0)?.toInt()
            val limit = req.queryParams()?.get("limit")?.get(0)?.toInt() ?: 8
            val offset = ((page ?: 1) - 1) * 8
            tagService.getAllPostByTagId(req.pathVariable("tagId").toInt(), offset, limit)
        }.flatMap {
            ok().body(
                Mono.just(it)
            )
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                Mono.just(it)
            )
        }
}



