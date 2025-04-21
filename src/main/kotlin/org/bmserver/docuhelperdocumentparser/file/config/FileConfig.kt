package org.bmserver.docuhelperdocumentparser.file.config

import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class FileConfig {

    @Bean
    fun fileClient():RestTemplate {
        return RestTemplateBuilder()
            .build()
    }
}