package com.ydh.jigglog.config

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate

@Configuration
class ElasticSearchConfig(
    @param:Value("\${elasticsearch.host}") private val host: String? = null,
    @param:Value("\${elasticsearch.port}") private val port: Int? = 0,
) {
    @Bean
    fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration: ClientConfiguration = ClientConfiguration.builder()
            .connectedTo("$host:$port")
            .usingSsl() // connect to https
            .build()
        return RestClients.create(clientConfiguration).rest()
    }

    @Bean
    fun elasticsearchOperations(): ElasticsearchOperations {
        return ElasticsearchRestTemplate(elasticsearchClient())
    }
}