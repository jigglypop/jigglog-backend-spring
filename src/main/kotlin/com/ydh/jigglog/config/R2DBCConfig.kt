package com.ydh.jigglog.config

import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration
import dev.miku.r2dbc.mysql.MySqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories
open class R2DBCConfig: AbstractR2dbcConfiguration(
) {

    @Value("\${spring.r2dbc.url}")
    lateinit var url: String

    @Value("\${spring.r2dbc.username}")
    lateinit var username: String

    @Value("\${spring.r2dbc.password}")
    lateinit var password: String

    @Value("\${spring.r2dbc.database}")
    lateinit var database: String

    @Value("\${spring.r2dbc.port}")
    lateinit var port: String

    @Bean
    open override fun connectionFactory(): ConnectionFactory {

        return MySqlConnectionFactory.from(
            MySqlConnectionConfiguration.builder()
                .host(url)
                .password(password)
                .port(port.toInt())
                .database(database)
                .username(username)
                .build()
        )
    }
}