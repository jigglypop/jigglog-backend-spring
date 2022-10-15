package com.ydh.jigglog.service

import com.ydh.jigglog.domain.dto.*
import com.ydh.jigglog.repository.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class PortfolioService (
    @Autowired private val portfolioRepository: PortfolioRepository,
    @Autowired private val iconSetRepository: IconSetRepository,
    @Autowired private val imageUrlRepository: ImageUrlRepository

) {
    companion object {
        private val logger = LoggerFactory.getLogger(PortfolioService::class.java)
    }
    // 포트폴리오 모두 가져오기
    fun getPortfolioAll(): Mono<List<PortfolioDTO>> {
        return Mono.just("portfolio")
            .flatMap {
                portfolioRepository.findAllByCategoryId(11).collectList().toMono()
            }.flatMap {
                val portfolio_idx = mutableListOf<Int>()
                val portfolio_map = mutableMapOf<Int, PortfolioDTO>()
                for (portfolio in it) {
                    portfolio_idx.add(portfolio.id!!)
                    portfolio_map[portfolio.id!!] = PortfolioDTO(
                        id = portfolio.id,
                        title = portfolio.title,
                        summary = portfolio.summary,
                        content = portfolio.content,
                        images = portfolio.images,
                        viewcount = portfolio.viewcount,
                        site = portfolio.site,
                        createdAt = portfolio.createdAt,
                        updatedAt = portfolio.updatedAt,
                        iconsets = mutableListOf(),
                        imageurls = mutableListOf(),
                    )
                }
                Mono.zip(
                    iconSetRepository.findAllByPostIdIn(portfolio_idx).collectList().toMono(),
                    imageUrlRepository.findAllByPostIdIn(portfolio_idx).collectList().toMono(),
                    portfolio_map.toMono()
                )
            }.flatMap {
                val iconSets = it.t1
                val imageUrls = it.t2
                val portfolio_map = it.t3
                for (iconSet in iconSets) {
                    portfolio_map[iconSet.postId]?.iconsets?.add(iconSet)
                }
                for (imageUrl in imageUrls) {
                    portfolio_map[imageUrl.postId]?.imageurls?.add(imageUrl)
                }
                val results = mutableListOf<PortfolioDTO>()
                for (i in portfolio_map.keys) {
                    results.add(portfolio_map[i]!!)
                }
                results.toMono()
            }
    }
    // 포트폴리오 (유저, 아이콘셋) 가져오기
    fun getPortfolio(portfolioId: Int): Mono<PortfolioDTO?> {
        return Mono.just(portfolioId)
        .flatMap {
            portfolioRepository.existsById(it)
        }.flatMap { isExist ->
            if (!isExist) {
                throw error("포트폴리오가 없습니다")
            } else {
                portfolioRepository.findById(portfolioId)
                    .flatMap { portfolio ->
                        Mono.zip(
                            portfolio.toMono(),
                            // tag
                            iconSetRepository.findByPostId(portfolioId).collectList().toMono(),
                            // category
                            imageUrlRepository.findByPostId(portfolioId).collectList().toMono(),
                        )
                    }.flatMap {
                        val portfolio = it.t1
                        val iconSets = it.t2
                        val imageUrls = it.t3
                        PortfolioDTO(
                            id = portfolio.id,
                            title = portfolio.title,
                            summary = portfolio.summary,
                            content = portfolio.content,
                            images = portfolio.images,
                            viewcount = portfolio.viewcount,
                            site = portfolio.site,
                            createdAt = portfolio.createdAt,
                            updatedAt = portfolio.updatedAt,
                            imageurls = imageUrls,
                            iconsets = iconSets
                        ).toMono()
                    }
            }
        }
    }

}

