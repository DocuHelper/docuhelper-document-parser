package org.bmserver.docuhelperdocumentparser.file

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.FileNotFoundException
import java.net.URI
import java.net.URL
import java.util.UUID

@Component
class FileService(
    private val fileClient: RestTemplate
) {
    fun getFileDownloadURL(uuid: UUID): URL {
        val fileDonwloadUrl = fileClient.getForObject("/file/${uuid}", String::class.java)
            ?: throw FileNotFoundException("No download URL returned for file $uuid")

        val sanitized = fileDonwloadUrl.trim().removeSurrounding("\"")
        return URI.create(sanitized).toURL()
    }
}