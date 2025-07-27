package com.ydh.signight

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class SignightBackendApplication

fun main(args: Array<String>) {
	runApplication<SignightBackendApplication>(*args)
} 