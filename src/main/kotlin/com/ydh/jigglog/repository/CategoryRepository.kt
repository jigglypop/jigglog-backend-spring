package com.ydh.jigglog.repository

import com.ydh.jigglog.domain.dto.CategoryDTO
import com.ydh.jigglog.domain.entity.Category
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CategoryRepository: ReactiveCrudRepository<Category, Int> {
    fun existsByTitle(title: String): Mono<Boolean>
    fun findByTitle(title: String): Mono<Category>

    @Query(
    """SELECT categories.*
            FROM ( 
                SELECT id, title, thumbnail, (
                    SELECT COUNT(id) 
                    FROM post 
                    WHERE post.categoryId = category.id 
                ) as posts 
                FROM category 
                WHERE id != 11 AND id != 1
            ) as categories
        WHERE posts != 0; """)
    fun findAllAndCount(): Flux<CategoryDTO>

    @Query("""
            SELECT
                c.id,
                c.title,
                c.imageUrl,
                COUNT(p.id) as postCount
            FROM
                category c
            LEFT JOIN
                post p ON c.id = p.categoryId
            WHERE
                c.id NOT IN (11, 1)
            GROUP BY
                c.id, c.title, c.imageUrl
            HAVING
                postCount != 0;
    """)
    fun findCountAll(): Flux<CategoryDTO>
}