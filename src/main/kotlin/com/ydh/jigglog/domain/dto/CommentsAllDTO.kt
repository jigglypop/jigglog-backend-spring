package com.ydh.jigglog.domain.dto

import java.time.LocalDateTime

class CommentsAllDTO (
    var comment_id: Int = 0,
    var comment_content: String? = "",
    var comment_createdat: LocalDateTime? = LocalDateTime.now(),
    var comment_userid: Int? = 0,
    var comment_postid: Int? = 0,
    var comment_username: String? = "",
    var comment_hashedpassword: String? = "",
    var comment_email: String? = "",
    var comment_imageurl: String? = "",
    var comment_githuburl: String? = "",
    var comment_summary: String? = "",
    var recomment_id: Int = 0,
    var recomment_content: String? = "",
    var recomment_createdat: LocalDateTime? = LocalDateTime.now(),
    var recomment_userid: Int? = 0,
    var recomment_commentid: Int? = 0,
    var recomment_username: String? = "",
    var recomment_hashedpassword: String? = "",
    var recomment_email: String? = "",
    var recomment_imageurl: String? = "",
    var recomment_githuburl: String? = "",
    var recomment_summary: String? = ""
)
