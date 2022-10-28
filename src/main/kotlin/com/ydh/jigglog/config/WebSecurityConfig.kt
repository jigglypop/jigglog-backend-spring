package com.ydh.jigglog.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache
import org.springframework.web.cors.CorsConfiguration
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfig {

    companion object {
        private val logger = LoggerFactory.getLogger(WebSecurityConfig::class.java)
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.cors().configurationSource { request ->
            val cors = CorsConfiguration()
            cors.allowedOrigins = listOf("http://localhost:3000", "https://jigglog.com")
            cors.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            cors.allowedHeaders = listOf("*")
            cors.exposedHeaders = listOf("Authorization")
            cors
        }
        return http
            .requestCache()
            .requestCache(NoOpServerRequestCache.getInstance()).and()
            .exceptionHandling()
            .authenticationEntryPoint { swe, e ->
                logger.error(e.message)
                Mono.fromRunnable { swe.response.statusCode = HttpStatus.UNAUTHORIZED } }
            .accessDeniedHandler { swe, e ->
                logger.error(e.message)
                Mono.fromRunnable { swe.response.statusCode = HttpStatus.FORBIDDEN } }
            .and()
            .requestCache().requestCache(NoOpServerRequestCache.getInstance()).and()
            .csrf().disable()
            .formLogin().disable()
            .authorizeExchange()
            .pathMatchers(HttpMethod.OPTIONS).permitAll()
            .anyExchange().permitAll()
            .and()
            .build()
    }
}