package com.ydh.jigglog.config

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ydh.jigglog.domain.dto.CategoryDTO
import com.ydh.jigglog.domain.dto.CategoryListDTO
import com.ydh.jigglog.domain.entity.Category
import com.ydh.jigglog.domain.entity.Tag
import com.ydh.jigglog.domain.entity.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
open class RedisConfig {
	@Value("\${spring.redis.port}")
	lateinit var port: String

	@Value("\${spring.redis.host}")
	lateinit var host: String

	/**
	 * ReactiveRedisTemplate를 생성하기 위한 공통 로직을 선언한 메소드
	 * @param factory Lettuce를 이용한 Non-blocking connection factory
	 * @param clazz ReactiveRedisTemplate에서 사용할 클래스
	 */
	private fun <T> commonReactiveRedisTemplate(
		factory: ReactiveRedisConnectionFactory?,
		clazz: Class<T>
	): ReactiveRedisTemplate<String, T> {
		val keySerializer = StringRedisSerializer()
		val redisSerializer = Jackson2JsonRedisSerializer(clazz)
			.apply {
				setObjectMapper(
					jacksonObjectMapper()
						.registerModule(JavaTimeModule())
				)
			}

		val serializationContext = RedisSerializationContext
			.newSerializationContext<String, T>()
			.key(keySerializer)
			.hashKey(keySerializer)
			.value(redisSerializer)
			.hashValue(redisSerializer)
			.build()

		return ReactiveRedisTemplate(factory!!, serializationContext)
	}



	//  reactive redis template
	@Bean
	fun userReactiveRedisTemplate(
		factory: ReactiveRedisConnectionFactory,
	): ReactiveRedisTemplate<String, User> = commonReactiveRedisTemplate(factory, User::class.java)

	@Bean
	fun categoryReactiveRedisTemplate(
		factory: ReactiveRedisConnectionFactory,
	): ReactiveRedisTemplate<String, CategoryListDTO> = commonReactiveRedisTemplate(factory, CategoryListDTO::class.java)

	@Bean
	fun tagReactiveRedisTemplate(
		factory: ReactiveRedisConnectionFactory,
	): ReactiveRedisTemplate<String, Tag> = commonReactiveRedisTemplate(factory, Tag::class.java)
}