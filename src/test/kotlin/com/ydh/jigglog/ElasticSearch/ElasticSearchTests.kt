package com.ydh.jigglog.ElasticSearch

import com.ydh.jigglog.domain.domain.PostDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ElasticSearchTests(
	@Autowired private val elasticPostRepository: ElasticPostRepository
	) {

	@Test
	@DisplayName("엘라스틱서치 저장 테스트")
	fun save() {
		val post = PostDomain(1, "연습용")
		elasticPostRepository.save(post)
	}

	@Test
	@DisplayName("엘라스틱서치 조회 테스트")
	fun findById() {
		// 저장
		val savedPost = PostDomain(1, "연습용")
		elasticPostRepository.save(savedPost)
		// 조회
		val searchedPost = elasticPostRepository.findById(1).orElseGet(null)
		// 테스트
		assertNotNull(searchedPost)
		assertEquals(savedPost.id, searchedPost.id)
		assertEquals(savedPost.title, searchedPost.title)
	}
}
