package com.ydh.jigglog.domain.dto

import com.ydh.jigglog.domain.entity.User

class ReCommentDTO (
    var id: Int = 0,
    var content: String? = "",
    var user: User? = null,
)
