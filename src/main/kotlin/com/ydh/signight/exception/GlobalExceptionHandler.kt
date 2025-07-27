package com.ydh.signight.exception

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono
import com.fasterxml.jackson.databind.ObjectMapper

@Component
@Order(-1) // 높은 우선순위
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper
) : WebExceptionHandler {
    
    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
    
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val response = exchange.response
        
        val (status, errorResponse) = when (ex) {
            is SignightException -> handleSignightException(ex)
            is IllegalArgumentException -> handleValidationException(ex)
            else -> handleGenericException(ex)
        }
        
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON
        
        val dataBuffer = response.bufferFactory().wrap(
            objectMapper.writeValueAsBytes(errorResponse)
        )
        
        return response.writeWith(Mono.just(dataBuffer))
    }
    
    private fun handleSignightException(ex: SignightException): Pair<HttpStatus, ErrorResponse> {
        val status = when (ex) {
            is AuthenticationException, is InvalidPasswordException -> HttpStatus.UNAUTHORIZED
            is UserNotFoundException, is PostNotFoundException, is CommentNotFoundException -> HttpStatus.NOT_FOUND
            is DuplicateUsernameException -> HttpStatus.CONFLICT
            is UnauthorizedException -> HttpStatus.FORBIDDEN
            is ValidationException -> HttpStatus.BAD_REQUEST
        }
        
        logger.warn("Business exception occurred: ${ex.errorCode} - ${ex.message}")
        
        return status to ErrorResponse(
            errorCode = ex.errorCode,
            message = ex.message ?: "처리 중 오류가 발생했습니다"
        )
    }
    
    private fun handleValidationException(ex: IllegalArgumentException): Pair<HttpStatus, ErrorResponse> {
        logger.warn("Validation error: ${ex.message}")
        
        return HttpStatus.BAD_REQUEST to ErrorResponse(
            errorCode = "VALIDATION_ERROR",
            message = ex.message ?: "입력값이 유효하지 않습니다"
        )
    }
    
    private fun handleGenericException(ex: Throwable): Pair<HttpStatus, ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        
        return HttpStatus.INTERNAL_SERVER_ERROR to ErrorResponse(
            errorCode = "INTERNAL_ERROR",
            message = "서버 오류가 발생했습니다"
        )
    }
}

data class ErrorResponse(
    val errorCode: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) 