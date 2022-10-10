package com.ydh.jigglog.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

class Upload(
    @Column("originalname")
    var originalname: String? = "",
    @Column("location")
    var location: String? = "",
)

