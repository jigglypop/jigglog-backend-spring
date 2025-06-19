package com.ydh.jigglog.domain.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

class PostDomain(
    @Id
    var id: Int = 0,
    @Column("title")
    var title: String? = "",
    @Column("summary")
    var summary: String? = "",
    @Column("content")
    var content: String? = "",
    @Column("images")
    var images: String? = "",
    @Column("viewcount")
    var viewcount: Int = 0,
)