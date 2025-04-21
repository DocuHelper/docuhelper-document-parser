package org.bmserver.docuhelperdocumentparser.kafka.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConsumerConfig {
    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<LinkedHashMap<String, String>, LinkedHashMap<String, String>>) =
        ConcurrentKafkaListenerContainerFactory<LinkedHashMap<String, String>, LinkedHashMap<String, String>>().also {
            it.consumerFactory = consumerFactory
        }


    @Bean
    fun consumerFactory(props: KafkaProperties): DefaultKafkaConsumerFactory<LinkedHashMap<String, String>, LinkedHashMap<String, String>> {
        val consumerProps = props.buildConsumerProperties()
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        consumerProps[JsonDeserializer.KEY_DEFAULT_TYPE] = "java.util.LinkedHashMap"

        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        consumerProps[JsonDeserializer.VALUE_DEFAULT_TYPE] = "java.util.LinkedHashMap"

        consumerProps[JsonDeserializer.USE_TYPE_INFO_HEADERS] = false
        consumerProps[JsonDeserializer.TRUSTED_PACKAGES] = "*"

        return DefaultKafkaConsumerFactory(
            consumerProps,
        )
    }
}