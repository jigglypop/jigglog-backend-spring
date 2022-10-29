package com.ydh.jigglog.domain.dto

import com.ydh.jigglog.domain.entity.Category
import com.ydh.jigglog.domain.entity.Comment
import com.ydh.jigglog.domain.entity.Tag
import com.ydh.jigglog.domain.entity.User
import java.time.LocalDateTime

class PostInCategoryDTO (
    var id: Int = 0,
    var title: String? = "",
    var summary: String? = "",
    var images: String? = "",
    var viewcount: Int = 0,
    var postcount: Int = 0,
    var commentcount: Int = 0,
    var last: Int = 0,
    var createdAt: LocalDateTime? = LocalDateTime.now(),
    var user: UserInPostCategoryDTO? = null,
)