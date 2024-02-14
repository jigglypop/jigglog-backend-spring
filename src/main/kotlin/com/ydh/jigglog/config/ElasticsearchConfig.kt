package com.ydh.jigglog.config

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories


@Configuration
@EnableElasticsearchRepositories
class ElasticsearchConfig() : AbstractElasticsearchConfiguration() {

    @Value("\${elasticsearch.host}")
    lateinit var host: String
    @Value("\${elasticsearch.port}")
    lateinit var port: String

    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration: ClientConfiguration = ClientConfiguration.builder()
            .connectedTo("$host:$port")
            .build();
        return RestClients.create(clientConfiguration).rest();
    }

//    @Bean
//    fun elasticsearchTemplate(): ElasticsearchOperations? {
//        return ElasticsearchRestTemplate(elasticsearchClient())
//    }
}