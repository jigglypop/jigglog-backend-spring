package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.UserDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

//@Service
class ExternalAuthService(
    private val webClient: WebClient,
    @Value("\${auth.service.url:http://localhost:8081}") 
    private val authServiceUrl: String
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ExternalAuthService::class.java)
    }

    data class TokenValidationRequest(val token: String)
    data class TokenValidationResponse(
        val valid: Boolean,
        val user: UserInfo? = null,
        val error: String? = null
    )
    
    data class UserInfo(
        val id: Long,
        val username: String,
        val email: String? = null,
        val imageUrl: String? = null,
        val githubUrl: String? = null,
        val summary: String? = null,
        val spiffeId: String? = null
    )

    fun validateToken(token: String): Mono<TokenValidationResponse> {
        val cleanToken = token.removePrefix("Bearer ")
        
        return webClient
            .post()
            .uri("$authServiceUrl/api/auth/validate")
            .bodyValue(TokenValidationRequest(cleanToken))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError) { response ->
                logger.warn("Token validation failed with status: ${response.statusCode()}")
                Mono.error(RuntimeException("Invalid token"))
            }
            .onStatus(HttpStatus::is5xxServerError) { response ->
                logger.error("Auth service error with status: ${response.statusCode()}")
                Mono.error(RuntimeException("Auth service unavailable"))
            }
            .bodyToMono<TokenValidationResponse>()
            .timeout(Duration.ofSeconds(5))
            .onErrorReturn(
                TokenValidationResponse(
                    valid = false, 
                    error = "Auth service timeout or error"
                )
            )
            .doOnNext { response ->
                if (response.valid) {
                    logger.debug("Token validated successfully for user: ${response.user?.username}")
                } else {
                    logger.warn("Token validation failed: ${response.error}")
                }
            }
    }

    fun getCurrentUser(token: String): Mono<UserDTO> {
        val cleanToken = token.removePrefix("Bearer ")
        
        return webClient
            .get()
            .uri("$authServiceUrl/api/auth/me")
            .header("Authorization", "Bearer $cleanToken")
            .retrieve()
            .onStatus(HttpStatus::isError) { response ->
                logger.error("Failed to get current user: ${response.statusCode()}")
                Mono.error(RuntimeException("Failed to get user info"))
            }
            .bodyToMono<UserInfo>()
            .map { userInfo ->
                UserDTO(
                    id = userInfo.id.toInt(),
                    username = userInfo.username,
                    email = userInfo.email,
                    imageUrl = userInfo.imageUrl,
                    githubUrl = userInfo.githubUrl,
                    summary = userInfo.summary
                )
            }
            .timeout(Duration.ofSeconds(5))
            .doOnSuccess { user ->
                logger.debug("Retrieved user info for: ${user.username}")
            }
            .doOnError { error ->
                logger.error("Failed to retrieve user info", error)
            }
    }

    fun checkAuthServiceHealth(): Mono<Boolean> {
        return webClient
            .get()
            .uri("$authServiceUrl/api/auth/health")
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .map { response ->
                response["status"] == "UP"
            }
            .timeout(Duration.ofSeconds(3))
            .onErrorReturn(false)
            .doOnNext { isHealthy ->
                if (isHealthy) {
                    logger.debug("Auth service is healthy")
                } else {
                    logger.warn("Auth service is not healthy")
                }
            }
    }
} 