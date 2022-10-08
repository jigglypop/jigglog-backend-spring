package com.ydh.jigglog.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "user")
class User(
    @Id
    var id: Int = 0,
    @Column("username")
    val username: String? = "",
    @Column("email")
    val email: String? = "",
    @Column("hashedPassword")
    var hashedPassword: String? = "",
    @Column("imageUrl")
    val imageUrl: String? = "",
    @Column("githubUrl")
    val githubUrl: String? = "",
    @Column("summary")
    val summary: String? = ""
)


class UserForm {
    var username: String?  = ""
    var password: String? = ""
}