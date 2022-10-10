package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.PostDTO
import com.ydh.jigglog.domain.dto.PostFormDTO
import com.ydh.jigglog.domain.entity.*
import com.ydh.jigglog.repository.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class PostService (
    @Autowired private val postRepository: PostRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val categoryRepository: CategoryRepository,
    @Autowired private val postToTagRepository: PostToTagRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PostService::class.java)
    }
    // 포스트 생성
    fun createPost(user: User, postForm: PostFormDTO, category: Category, tags: List<Tag>): Mono<PostDTO> {
        return postRepository.save(
            Post(
                title = postForm.title,
                summary = postForm.summary,
                content = postForm.content,
                images = postForm.images,
                userId = user.id,
                categoryId = category.id,
            )
        // 태그 조인
        ).flatMap { post ->
            var postToTags = mutableListOf<PostToTag>()
            for (tag in tags) {
                postToTags.add(
                    PostToTag(
                        postId = post.id,
                        tagId = tag.id
                    )
                )
            }
            Mono.zip(
                postToTagRepository.saveAll(postToTags).collectList().toMono(),
                post.toMono()
            )
        // 결과
        }.flatMap {
            val post = it.t2
            getPost(post.id).toMono()
        }
    }

    // 포스트 (유저, 태그) 가져오기
    fun getPost(postId: Int): Mono<PostDTO?> {
        return Mono.just(postId)
        .flatMap {
            postRepository.existsById(it)
        }.flatMap { isExist ->
            if (!isExist) {
                throw error("포스트가 없습니다")
            } else {
                postRepository.findById(postId)
                    .flatMap { post ->
                        logger.info(post.title)
                        Mono.zip(
                            post.toMono(),
                            // tag
                            postRepository.findTagsByPostId(postId).collectList().toMono(),
                            // user
                            userRepository.findById(post.userId!!)
                                .flatMap {
                                        user ->
                                        user.apply {
                                            hashedPassword = ""
                                }.toMono()
                            }.toMono(),
                            // category
                            categoryRepository.findById(post.categoryId!!).toMono(),
                        )
                    }.flatMap {
                        val post = it.t1
                        val tags = it.t2
                        val user = it.t3
                        val category = it.t4
                        PostDTO(
                            id = post.id,
                            title = post.title,
                            summary = post.summary,
                            content = post.content,
                            images = post.images,
                            viewcount = post.viewcount,
                            site = post.site,
                            createdAt = post.createdAt,
                            updatedAt = post.updatedAt,
                            user = user,
                            category = category,
                            tags = tags
                        ).toMono()
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

