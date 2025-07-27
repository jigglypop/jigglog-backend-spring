package com.ydh.signight.util

import com.ydh.signight.exception.*
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

object ResponseHelper {
    private val logger = LoggerFactory.getLogger(ResponseHelper::class.java)
    
    /**
     * 성공 응답과 함께 JWT 토큰을 헤더에 포함
     */
    fun <T : Any> successWithToken(token: String, body: T): Mono<ServerResponse> {
        return ServerResponse.ok()
            .header("token", "Bearer $token")
            .bodyValue(body)
    }
    
    /**
     * 단순 성공 응답
     */
    fun <T : Any> success(body: T): Mono<ServerResponse> {
        return ServerResponse.ok()
            .bodyValue(body)
    }
    
    /**
     * 에러 응답 - 더 이상 예외 객체 전체를 노출하지 않음
     */
    fun errorResponse(error: Throwable): Mono<ServerResponse> {
        logger.error("Request failed", error)
        
        val (errorCode, errorMessage) = when (error) {
            is SignightException -> {
                // 커스텀 예외는 에러 코드와 메시지를 그대로 사용
                error.errorCode to (error.message ?: "처리 중 오류가 발생했습니다")
            }
            is IllegalArgumentException -> {
                "INVALID_ARGUMENT" to (error.message ?: "잘못된 요청입니다")
            }
            is NoSuchElementException -> {
                "NOT_FOUND" to "요청한 리소스를 찾을 수 없습니다"
            }
            is IllegalStateException -> {
                "INVALID_STATE" to (error.message ?: "잘못된 상태입니다")
            }
            else -> {
                // 예상하지 못한 에러는 로그에만 상세 내용 기록
                logger.error("Unexpected error: ${error.javaClass.simpleName}", error)
                "INTERNAL_ERROR" to "서버 오류가 발생했습니다"
            }
        }
        
        return ServerResponse.badRequest()
            .bodyValue(mapOf(
                "errorCode" to errorCode,
                "error" to errorMessage,
                "timestamp" to System.currentTimeMillis()
            ))
    }
    
    /**
     * 에러 메시지로 직접 응답
     */
    fun errorResponse(message: String): Mono<ServerResponse> {
        return ServerResponse.badRequest()
            .bodyValue(mapOf(
                "errorCode" to "VALIDATION_ERROR",
                "error" to message,
                "timestamp" to System.currentTimeMillis()
            ))
    }
} 