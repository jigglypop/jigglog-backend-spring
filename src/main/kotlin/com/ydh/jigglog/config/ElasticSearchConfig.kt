package com.ydh.jigglog.config

import com.ydh.jigglog.service.AuthService
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration
@EnableElasticsearchRepositories()
@ComponentScan()
class ElasticSearchConfig(
    @param:Value("\${elasticsearch.host}") private val host: String? = null,
    @param:Value("\${elasticsearch.port}") private val port: Int? = 0,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ElasticSearchConfig::class.java)
    }
    @Bean
    fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration: ClientConfiguration = ClientConfiguration.builder()
            .connectedTo("localhost:9020")
            .usingSsl() // connect to https
            .build()
        logger.info(host)
        return RestClients.create(clientConfiguration).rest()
    }

    @Bean
    fun elasticsearchOperations(): ElasticsearchOperations {
        return ElasticsearchRestTemplate(elasticsearchClient())
    }
}