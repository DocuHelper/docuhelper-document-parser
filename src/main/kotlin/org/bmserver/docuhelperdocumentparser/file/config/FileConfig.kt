package org.bmserver.docuhelperdocumentparser.file.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class FileConfig {
    @Value("\${docuhelper.file.endpoint}")
    private lateinit var docuhelperFileEndpoint: String

    @Bean
    fun fileClient():RestTemplate {
        return RestTemplateBuilder()
            .rootUri(docuhelperFileEndpoint)
            .build()
    }
}