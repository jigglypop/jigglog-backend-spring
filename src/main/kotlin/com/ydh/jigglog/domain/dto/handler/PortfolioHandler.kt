package com.ydh.jigglog.domain.dto.handler

import com.ydh.jigglog.service.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class PortfolioHandler(
    @Autowired private val portfolioService: PortfolioService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PortfolioHandler::class.java)
    }
    // 모두 가져오기
    fun getAll(req: ServerRequest) = Mono.just(req)
        .flatMap {
            portfolioService.getPortfolioAll().toMono()
        }
        // 병렬 실행 : 폼 체크, 관리자 체크
        .flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
    // 단일 포트폴리오 가져오기
    fun get(req: ServerRequest) = Mono.just(req)
        .flatMap {
            portfolioService.getPortfolio(req.pathVariable("portfolioId").toInt()).toMono()
        }
        // 병렬 실행 : 폼 체크, 관리자 체크
        .flatMap {
            ok().body(it.toMono())
        }.onErrorResume(Exception::class.java) {
            badRequest().body(
                mapOf("message" to it.message).toMono()
            )
        }
}
