package com.ydh.jigglog.service

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.ydh.jigglog.domain.entity.Upload
import com.ydh.jigglog.handler.UploadHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*

@Service
class UploadService (
    private val s3Client: AmazonS3Client
    ) {
        companion object {
            private val logger = LoggerFactory.getLogger(UploadHandler::class.java)
        }
        @Value("\${cloud.aws.s3.bucket}")
        lateinit var bucket: String

        @Value("\${cloud.aws.s3.dir}")
        lateinit var dir: String

        @Throws(IOException::class)
        fun upload(file: FilePart): Mono<Upload> {
            val fileName = UUID.randomUUID().toString() + "-" + file.filename()
            val objMeta = ObjectMetadata()
            val bytes = DataBufferUtils.join(file.content())
                .map { dataBuffer: DataBuffer ->
                    dataBuffer.asByteBuffer().array()
                }
            return bytes.flatMap { bytes ->
                objMeta.contentLength = bytes.size.toLong()
                val byteArrayIs = ByteArrayInputStream(bytes)
                s3Client.putObject(
                    PutObjectRequest(bucket, "images/$fileName", byteArrayIs, objMeta)
                        .withCannedAcl(CannedAccessControlList.PublicRead))
                Mono.zip(
                    s3Client.getUrl(bucket, "images/$fileName").toString().toMono(),
                    fileName.toMono()
                )
            }.flatMap {
                Upload(originalname = it.t2, location = it.t1).toMono()
            }
        }
    }