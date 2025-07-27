package com.ydh.signight.router

import com.ydh.signight.handler.PortfolioHandler
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.router

@Component
class PortfolioRouter(private val handler: PortfolioHandler) {

    @Bean
    fun portfolioRouterFunction() = router {
        "/api/portfolio".nest {
            GET("", handler::getAll)
            GET("/{portfolioId}", handler::get)
        }
    }

}