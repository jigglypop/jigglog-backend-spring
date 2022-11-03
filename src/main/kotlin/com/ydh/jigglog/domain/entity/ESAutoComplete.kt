package com.ydh.jigglog.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.relational.core.mapping.Column

@Document(indexName = "posttitle")
class ESAutoComplete(
    @Id
    var id: Int = 0,
    @Column("title")
    var title: String? = "",
    @Column("content")
    var content: String? = "",
)