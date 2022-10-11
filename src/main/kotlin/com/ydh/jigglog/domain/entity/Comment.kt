package com.ydh.jigglog.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "comment")
class Comment(
    @Id
    var id: Int = 0,
    @Column("content")
    var content: String? = "",
    @Column("createdAt")
    var createdAt: LocalDateTime? = LocalDateTime.now(),
    // 유저
    @Column("userId")
    var userId: Int? = 0,
    // 포스트
    @Column("postId")
    var postId: Int? = 0,
)
