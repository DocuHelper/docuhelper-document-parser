package org.bmserver.docuhelperdocumentparser.kafka.common

import org.springframework.kafka.annotation.KafkaListener

abstract class DocuhelperApiEventListener<T>: BaseEventListener<T>() {
    @KafkaListener(groupId = "docuhelper-document-parser", topics = ["docuhelper-api"])
    override fun listen(event: Map<String, Any>, eventType: String) {
        super.listen(event, eventType)
    }
}