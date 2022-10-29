package com.ydh.jigglog.domain.dto

import java.time.LocalDateTime

class PostInCategoryInDTO (
    var id: Int = 0,
    var title: String? = "",
    var summary: String? = "",
    var images: String? = "",
    var viewcount: Int = 0,
    var postcount: Int = 0,
    var commentcount: Int = 0,
    var last: Int = 0,
    var createdat: LocalDateTime? = LocalDateTime.now(),
    var userid: Int = 0,
    var username: String?  = "",
    var imageurl: String? = "",
)