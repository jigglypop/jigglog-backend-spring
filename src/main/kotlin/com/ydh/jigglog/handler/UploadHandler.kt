package com.ydh.jigglog.handler

import com.ydh.jigglog.service.UploadService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class UploadHandler(@Autowired private val uploadService: UploadService) {

    fun uploadFile(request: ServerRequest): Mono<ServerResponse> {
        return request.multipartData()
            .flatMap { parts ->
                val filePart = parts.getFirst("file") as? org.springframework.http.codec.multipart.FilePart
                if (filePart != null) {
                    uploadService.uploadImage(filePart)
                        .flatMap { imageUrlDTO ->
                            ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(imageUrlDTO)
                        }
                } else {
                    ServerResponse.badRequest()
                        .bodyValue(mapOf("error" to "파일이 없습니다."))
                }
            }
            .onErrorResume { error ->
                ServerResponse.status(500)
                    .bodyValue(mapOf("error" to error.message))
            }
    }

    fun deleteFile(request: ServerRequest): Mono<ServerResponse> {
        val fileName = request.pathVariable("fileName")
        return uploadService.deleteImage(fileName)
            .flatMap { result ->
                ServerResponse.ok()
                    .bodyValue(mapOf("result" to result))
            }
            .onErrorResume { error ->
                ServerResponse.status(500)
                    .bodyValue(mapOf("error" to error.message))
            }
    }
}




