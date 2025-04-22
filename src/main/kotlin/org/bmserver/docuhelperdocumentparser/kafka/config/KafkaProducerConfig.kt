package org.bmserver.docuhelperdocumentparser.kafka.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.bmserver.docuhelperdocumentparser.core.domain.event.EventKey
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaProducerConfig {

    @Bean
    fun kafkaTemplate(props: KafkaProperties): KafkaTemplate<EventKey, Any> {
        val producerProps = props.buildProducerProperties()
        producerProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        producerProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        producerProps[JsonSerializer.ADD_TYPE_INFO_HEADERS] = false

        val producerFactory = DefaultKafkaProducerFactory<EventKey, Any>(producerProps)
        return KafkaTemplate(producerFactory)
    }

}