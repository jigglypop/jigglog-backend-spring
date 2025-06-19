package com.ydh.jigglog

import com.ydh.jigglog.config.TestConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@Import(TestConfig::class)
@TestPropertySource(properties = [
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password="
])
class SimpleTest {

    @Test
    fun `애플리케이션 컨텍스트가 정상적으로 로드된다`() {
        println("테스트가 성공적으로 실행되었습니다!")
    }

    @Test
    fun `기본 테스트 로직이 동작한다`() {
        val result = 1 + 1
        assert(result == 2) { "1 + 1은 2여야 합니다" }
    }
} 