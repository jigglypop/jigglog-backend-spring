package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.ReCommentFormDTO
import com.ydh.jigglog.domain.entity.ReComment
import com.ydh.jigglog.repository.ReCommentRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class ReCommentService (
    @Autowired private val recommentRepository: ReCommentRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReCommentService::class.java)
    }
    // 대댓글 생성
    fun createReComment(commentForm: ReCommentFormDTO, userId: Int, commentId: Int): Mono<ReComment> {
        return recommentRepository.save(
            ReComment(
                content = commentForm.content,
                userId = userId,
                commentId = commentId
            )
        )
    }
    // 코멘트 삭제
    fun deleteReComment(recommentId: Int): Mono<Boolean> {
        return recommentRepository.deleteById(recommentId).thenReturn(true)
    }
    // 단일 대댓글 가져오기
    fun getReComment(recommentId: Int): Mono<ReComment> {
        return recommentRepository.existsById(recommentId)
            .flatMap {  isExist ->
                if (isExist) {
                    recommentRepository.findById(recommentId)
                } else {
                    throw error("대댓글이 없습니다")
                }
            }
    }
}