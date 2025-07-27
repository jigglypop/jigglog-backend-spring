package com.ydh.signight.service

import com.ydh.signight.domain.dto.PostDTO
import com.ydh.signight.domain.dto.PostFormDTO
import com.ydh.signight.domain.dto.PostPathDTO
import com.ydh.signight.domain.dto.UpdateFormDTO
import com.ydh.signight.domain.entity.*
import com.ydh.signight.repository.*
import com.ydh.signight.exception.PostNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
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
        return postRepository.findById(postId)
            .switchIfEmpty(Mono.error(PostNotFoundException("포스트를 찾을 수 없습니다: $postId")))
    }
    
    // 포스트 패스 가져오기
    fun getPostPath(): Mono<List<PostPathDTO>> {
        return postRepository.findAll()
            .map { post ->
                PostPathDTO(
                    id = post.id,
                    title = post.title
                )
            }
            .collectList()
    }
    
    // 포스트 (유저, 태그) 가져오기 - N+1 문제 개선
    fun getPost(postId: Int): Mono<PostDTO?> {
        // 먼저 포스트 존재 여부 확인 및 조회수 증가를 한 번에 처리
        return postRepository.findById(postId)
            .switchIfEmpty(Mono.error(PostNotFoundException("포스트를 찾을 수 없습니다: $postId")))
            .flatMap { post ->
                // 조회수 증가
                post.viewcount++
                postRepository.save(post)
            }
            .flatMap { post ->
                // 연관 데이터를 병렬로 조회
                Mono.zip(
                    Mono.just(post),
                    postRepository.findTagsByPostId(postId).collectList(),
                    userRepository.findById(post.userId!!).map { user ->
                        user.apply { hashedPassword = "" }
                    },
                    categoryRepository.findById(post.categoryId!!)
                )
            }
            .map { tuple ->
                val post = tuple.t1
                val tags = tuple.t2
                val user = tuple.t3
                val category = tuple.t4
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
                )
            }
            .doOnError { error ->
                logger.error("Error fetching post with id: $postId", error)
            }
    }

    // 포스트 업데이트
    fun updatePost(post: Post, updateForm: UpdateFormDTO): Mono<PostDTO> {
        return Mono.just(post.apply {
            updateForm.title?.takeIf { it.isNotBlank() }?.let { title = it }
            updateForm.summary?.takeIf { it.isNotBlank() }?.let { summary = it }
            updateForm.content?.takeIf { it.isNotBlank() }?.let { content = it }
            updateForm.images?.takeIf { it.isNotBlank() }?.let { images = it }
        })
        .flatMap { updatedPost ->
            postRepository.save(updatedPost)
        }
        .flatMap { savedPost ->
            getPost(savedPost.id).toMono()
        }
    }

    // 포스트 삭제
    fun deletePost(postId: Int): Mono<Boolean> {
        logger.info("Deleting post with id: $postId")
        return postRepository.deleteById(postId)
            .thenReturn(true)
            .doOnSuccess { logger.info("Successfully deleted post with id: $postId") }
            .doOnError { error -> logger.error("Failed to delete post with id: $postId", error) }
    }
}

