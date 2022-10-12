package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.PostDTO
import com.ydh.jigglog.domain.dto.PostFormDTO
import com.ydh.jigglog.domain.dto.UpdateFormDTO
import com.ydh.jigglog.domain.entity.*
import com.ydh.jigglog.repository.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class PortfolioService (
    @Autowired private val postRepository: PostRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val categoryRepository: CategoryRepository,
    @Autowired private val postToTagRepository: PostToTagRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PortfolioService::class.java)
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

    // 포스트 업데이트
    fun updatePost(post: Post, updateForm: UpdateFormDTO): Mono<Post> {
        return Mono.just(updateForm
        ).flatMap { updateForm ->
            updateForm.toMono()
        }.flatMap { updateForm ->
            post.toMono()
        }
    }
}

