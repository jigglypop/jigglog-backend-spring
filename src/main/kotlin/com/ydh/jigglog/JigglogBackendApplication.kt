package com.ydh.jigglog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableCaching
@EnableR2dbcRepositories("com.ydh.jigglog.repository")
class JigglogBackendApplication

fun main(args: Array<String>) {
	runApplication<JigglogBackendApplication>(*args)
}
