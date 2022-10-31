package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.PostDTO
import com.ydh.jigglog.domain.dto.PostFormDTO
import com.ydh.jigglog.domain.dto.PostPathDTO
import com.ydh.jigglog.domain.dto.UpdateFormDTO
import com.ydh.jigglog.domain.entity.*
import com.ydh.jigglog.repository.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
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

    // 포스트만 가져오기
    fun getOnlyPost(postId: Int): Mono<Post> {
        return Mono.just(postId)
            .flatMap {
                postRepository.findById(postId).toMono()
            }
    }
    // 포스트 패스 가져오기
    fun getPostPath(): Mono<List<PostPathDTO>> {
        return Mono.just(true)
            .flatMap {
                postRepository.findAll().collectList().toMono()
            }.flatMap {
                val postPath = mutableListOf<PostPathDTO>()
                for (post in it) {
                    postPath.add(PostPathDTO(
                        id = post.id,
                        title = post.title
                    ))

                }
                postPath.toMono()
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
                        postRepository.save(post.apply {
                            viewcount++
                        }).toMono()
                    }.flatMap { post ->
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

    // 포스트 업데이트
    fun updatePost(post: Post, updateForm: UpdateFormDTO): Mono<PostDTO> {
        return Mono.just(updateForm
        ).flatMap { form ->
            postRepository.save(post.apply {
                if (form.title != "" && form.title != null) title = form.title
                if (form.summary != "" && form.summary != null) summary = form.summary
                if (form.content != "" && form.content != null) content = form.content
                if (form.images != "" && form.images != null) images = form.images
            })
        }.flatMap { post ->
            getPost(post.id).toMono()
        }
    }

    // 포스트 삭제
    fun deltePost(postId: Int): Mono<Boolean> {
        return postRepository.deleteById(postId).thenReturn(true)
    }
}

