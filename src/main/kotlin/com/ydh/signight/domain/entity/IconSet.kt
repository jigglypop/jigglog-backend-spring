package com.ydh.signight.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "icon_set")
class IconSet(
    @Id
    var id: Int = 0,
    @Column("title")
    var title: String? = "",
    // 포스트
    @Column("postId")
    var postId: Int? = 0,
)
