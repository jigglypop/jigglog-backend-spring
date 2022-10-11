package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.entity.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRepository: ReactiveCrudRepository<User, Int> {
    fun findByUsername(username: String) : Mono<User>
    fun existsByUsername(username: String): Mono<Boolean>
    fun findAllByIdIn(ids: List<Int>): Flux<User>
}