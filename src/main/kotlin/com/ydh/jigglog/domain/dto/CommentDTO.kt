package com.ydh.jigglog.domain.dto

import java.time.LocalDateTime

class CommentDTO (
    var id: Int = 0,
    var content: String? = "",
    var user: UserDTO? = null,
    var recomments: MutableList<ReCommentDTO> = mutableListOf<ReCommentDTO>(),
    var createdAt: LocalDateTime? = LocalDateTime.now(),
    var userId: Int = 0,
    var commentId: Int = 0,
)
