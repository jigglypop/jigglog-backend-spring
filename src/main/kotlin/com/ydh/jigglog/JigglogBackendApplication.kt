package com.ydh.jigglog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class JigglogBackendApplication

fun main(args: Array<String>) {
	runApplication<JigglogBackendApplication>(*args)
}
