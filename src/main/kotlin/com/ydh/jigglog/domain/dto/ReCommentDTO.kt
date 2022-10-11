package com.ydh.jigglog.domain.dto

import java.time.LocalDateTime

class ReCommentDTO (
    var id: Int = 0,
    var content: String? = "",
    var user: UserDTO? = null,
    var createdAt: LocalDateTime? = LocalDateTime.now(),
    var userId: Int = 0,
    var commentId: Int = 0,
)
