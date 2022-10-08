package com.ydh.jigglog.service

import com.ydh.jigglog.domain.Post
import com.ydh.jigglog.domain.Tag
import com.ydh.jigglog.handler.AuthHandler
import com.ydh.jigglog.repository.PostRepository
import com.ydh.jigglog.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class PostService (
    @Autowired private val postRepository: PostRepository,
    @Autowired private val userRepository: UserRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuthHandler::class.java)
    }
    // 포스트 생성
//    fun createPost(userId: Int, post: Post): Mono<Post> {
//        return postRepository.save(post.apply {
//            this.profile = profile
//        }).toMono()
//    }

    // 포스트 (유저, 태그) 가져오기
    fun getPost(postId: Int): Mono<Post?> {
        return Mono.just(postId).flatMap {
            postRepository.existsById(it)
        }.flatMap {
            if (!it) {
                throw error("포스트가 없습니다")
            } else {
                postRepository.findById(postId)
                    .flatMap { post ->
                        Mono.zip(
                            post.toMono(),
                            // tag
                            postRepository.findTagsByPostId(postId).collectList().toMono(),
                            // user
                            userRepository.findById(post.userId!!).flatMap {
                                it.apply {
                                    this.hashedPassword = ""
                                }.toMono()
                            }
                        )
                    }.flatMap {
                        it.t1.apply {
                            this.tags = it.t2
                            this.user = it.t3
                        }.toMono()
                    }
            }
        }
    }

    // 포스트의 태그 가져오기
    fun getTags(postId: Int): Flux<Tag> {
        return postRepository.findTagsByPostId(postId)
    }
//    // 포스트 업데이트
//    fun updatePost(post: Post, form: Post): Mono<Post> {
//        if (post != null) {
//            return post.toMono().flatMap {
//                postRepository.save(it.apply {
//                    this.title = form.title
//                    this.content = form.content
//                    this.updatedAt = LocalDateTime.now()
//                }).toMono()
//            }
//        } else {
//            return Mono.error(Exception("포스트가 없습니다."))
//        }
//    }
//
//    // 포스트 삭제
//    fun deletePost(postId: Int) {
//        return postRepository.deleteById(postId)
//    }
}