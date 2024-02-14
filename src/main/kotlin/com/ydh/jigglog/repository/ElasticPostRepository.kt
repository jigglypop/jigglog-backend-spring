package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.domain.PostDomain
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface ElasticPostRepository: ElasticsearchRepository<PostDomain, Int>
