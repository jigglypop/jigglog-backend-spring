package com.ydh.jigglog.domain.dto

import java.time.LocalDateTime

class EveryCommentDTO (
    var id: Int = 0,
    var content: String? = "",
    var createdat: LocalDateTime? = LocalDateTime.now(),
    var userid: Int? = 0,
    var postid: Int? = 0,
    var username: String? = "",
    var hashedpassword: String? = "",
    var email: String? = "",
    var imageurl: String? = "",
    var githuburl: String? = "",
    var summary: String? = ""
)
