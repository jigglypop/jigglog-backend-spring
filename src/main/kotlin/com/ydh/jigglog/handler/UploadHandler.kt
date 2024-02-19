package com.ydh.jigglog.handler

import com.fasterxml.jackson.annotation.JsonIgnore
import com.ydh.jigglog.service.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.File

@Component
class UploadHandler(
    @Autowired val uploadService: UploadService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UploadHandler::class.java)
    }

    fun upload(req: ServerRequest): Mono<ServerResponse> {
        return req.body(BodyExtractors.toMultipartData()).flatMap { parts ->
            val map: Map<String, Part> = parts.toSingleValueMap()
            val filePart : FilePart = map["img"]!! as FilePart
            uploadService.upload(filePart)
        }.flatMap {
            ok().body(
                it.toMono()
            )
        }.onErrorResume(Exception::class.java) {
            ServerResponse.badRequest().body(
                Mono.just(it)
            )
        }
    }
}




