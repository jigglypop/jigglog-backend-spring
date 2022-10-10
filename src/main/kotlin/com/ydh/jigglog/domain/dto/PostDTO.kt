package com.ydh.jigglog.domain.dto

import com.ydh.jigglog.domain.entity.Category
import com.ydh.jigglog.domain.entity.Tag
import com.ydh.jigglog.domain.entity.User
import java.time.LocalDateTime

class PostDTO (
    var id: Int = 0,
    var title: String? = "",
    var summary: String? = "",
    var content: String? = "",
    var images: String? = "",
    var viewcount: Int = 0,
    var site: String? = "",
    var createdAt: LocalDateTime? = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = LocalDateTime.now(),
    var user: User? = null,
    var category: Category? = null,
    var tags: List<Tag>? = null,
)