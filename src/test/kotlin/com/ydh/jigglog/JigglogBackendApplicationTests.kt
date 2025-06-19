package com.ydh.jigglog

import com.ydh.jigglog.config.TestConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@Import(TestConfig::class)
class JigglogBackendApplicationTests {

	@Test
	fun contextLoads() {
	}

}
