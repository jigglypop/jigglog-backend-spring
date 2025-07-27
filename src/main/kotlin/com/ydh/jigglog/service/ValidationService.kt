package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.UserFormDTO
import com.ydh.jigglog.repository.UserRepository
import com.ydh.jigglog.exception.ValidationException
import com.ydh.jigglog.exception.DuplicateUsernameException
import com.ydh.jigglog.exception.UserNotFoundException

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

    fun <T : Any>checkValidForm(mono: T, form: Map<String?, String?>): Mono<T> {
        val invalidFields = form.filter { it.value.isNullOrBlank() }
        
        // 밸리데이션을 만족할 경우
        return if (invalidFields.isEmpty()) {
            Mono.just(mono)
        // 아닐 경우
        } else {
            Mono.error(ValidationException("다음의 값을 입력해 주세요: ${invalidFields.keys.joinToString(", ")}"))
        }
    }

    fun checkValidUsername(userForm: UserFormDTO): Mono<UserFormDTO> {
        return userRepository.existsByUsername(userForm.username!!)
            .flatMap { exists ->
                if (exists) {
                    Mono.error(DuplicateUsernameException("이미 같은 이름의 유저가 있습니다: ${userForm.username}"))
                } else {
                    Mono.just(userForm)
                }
            }
    }

    fun checkNotValidUsername(userForm: UserFormDTO): Mono<UserFormDTO> {
        return userRepository.existsByUsername(userForm.username!!)
            .flatMap { exists ->
                if (exists) {
                    Mono.just(userForm)
                } else {
                    Mono.error(UserNotFoundException("해당 이름의 유저가 없습니다: ${userForm.username}"))
                }
            }
    }

    fun checkUsernameBoolean(userForm: UserFormDTO): Mono<Boolean> {
        return userRepository.existsByUsername(userForm.username!!)
            .doOnNext { exists ->
                logger.debug("Username '${userForm.username}' exists: $exists")
            }
    }
}



