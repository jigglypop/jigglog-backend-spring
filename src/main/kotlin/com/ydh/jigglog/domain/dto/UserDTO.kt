package com.ydh.jigglog.domain.dto

class UserDTO (
    var id: Int? = 0,
    var username: String?  = "",
    var hashedPassword: String? = "",
    var email: String? = "",
    var imageUrl: String? = "",
    var githubUrl: String? = "",
    var summary: String? = ""
)