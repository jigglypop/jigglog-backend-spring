package com.ydh.jigglog.domain

import org.springframework.core.metrics.StartupStep
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "post")
class Post(
    @Id
    var id: Int = 0,
    @Column("title")
    val title: String? = "",
    @Column("summary")
    val summary: String? = "",
    @Column("content")
    val content: String? = "",
    @Column("images")
    val images: String? = "",
    @Column("viewcount")
    var viewcount: Int = 0,
    @Column("site")
    val site: String? = "",
    @Column("createdAt")
    var createdAt: LocalDateTime? = LocalDateTime.now(),
    @Column("updatedAt")
    var updatedAt: LocalDateTime? = LocalDateTime.now(),
    // 유저
    @Column("userId")
    var userId: Int? = 0,
    @Column("user")
    var user: User? = null,
    // 태그
    @Column("tags")
    var tags: List<Tag>? = null,
)

