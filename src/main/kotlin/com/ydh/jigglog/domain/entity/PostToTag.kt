package com.ydh.jigglog.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "post_to_tag")
class PostToTag(
    @Id
    var id: Int = 0,
    @Column("postId")
    var postId: Int = 0,
    @Column("tagId")
    var tagId: Int = 0,
)

