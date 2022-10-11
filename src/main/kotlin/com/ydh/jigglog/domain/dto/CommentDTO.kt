package com.ydh.jigglog.domain.dto

import com.ydh.jigglog.domain.entity.ReComment
import com.ydh.jigglog.domain.entity.User

class CommentDTO (
    var id: Int = 0,
    var content: String? = "",
    var user: User? = null,
    var recomments: List<ReComment>? = null,
)
