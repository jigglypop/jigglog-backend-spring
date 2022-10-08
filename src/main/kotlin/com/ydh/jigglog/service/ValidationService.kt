package com.ydh.jigglog.service

import com.ydh.jigglog.domain.UserForm
import com.ydh.jigglog.repository.UserRepository

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class ValidationService (
    @Autowired private val userRepository: UserRepository
    ) {

    companion object {
        private val logger = LoggerFactory.getLogger(ValidationService::class.java)
    }

    fun checkValidForm(mono: UserForm, form: Map<String?, String?>): Mono<UserForm> {
        val valid = form.filter { it.value == null || it.value == "" }
        // 밸리데이션을 만족할 경우
        return if (valid.keys.isEmpty()) {
            Mono.just(mono)
        // 아닐 경우
        } else {
            throw Exception("다음의 값을 입력해 주세요: ${valid.keys.toString()}")
        }

    }

    fun checkValidUsername(userForm: UserForm): Mono<UserForm> {
        return userRepository.existsByUsername(userForm.username!!).flatMap {
            if (it) {
                throw error("이미 같은 이름의 유저가 있습니다")
            } else {
               userForm.toMono()
            }
        }
    }
}



