package org.bmserver.docuhelperdocumentparser.kafka.common

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.bmserver.docuhelperdocumentparser.core.domain.event.AbstractEvent
import org.bmserver.docuhelperdocumentparser.core.domain.event.EventKey
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val kafkaTemplate: KafkaTemplate<EventKey, Any>
) {

    fun publish(event:AbstractEvent) {
        val header = mutableListOf(
            RecordHeader("eventType", event::class.simpleName.toString().toByteArray())
        )
        val key = EventKey(eventType = event::class.simpleName.toString())

        val record = ProducerRecord<EventKey, Any>(
            "docuhelper-document-parser",
            null,
            key,
            event,
            header
        )

        kafkaTemplate.send(record)
    }
}