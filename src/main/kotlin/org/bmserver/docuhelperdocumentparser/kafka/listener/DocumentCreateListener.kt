package org.bmserver.docuhelperdocumentparser.kafka.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.bmserver.docuhelperdocumentparser.core.chunk.event.DocumentParse
import org.bmserver.docuhelperdocumentparser.core.chunk.event.DocumentParseComplete
import org.bmserver.docuhelperdocumentparser.core.chunk.event.DocumentParseFail
import org.bmserver.docuhelperdocumentparser.core.event.DocumentCreate
import org.bmserver.docuhelperdocumentparser.kafka.common.DocuhelperApiEventListener
import org.bmserver.docuhelperdocumentparser.kafka.common.EventPublisher
import org.bmserver.docuhelperdocumentparser.parser.ParserService
import org.springframework.stereotype.Component


val logger = KotlinLogging.logger { }

@Component
class DocumentCreateListener(
    private val parserService: ParserService,
    private val eventPublisher: EventPublisher,
) : DocuhelperApiEventListener<DocumentCreate>() {

    override suspend fun handle(event: DocumentCreate) {
        try {
            logger.info { "Handle DocuhelperApiEventListener - ${event.document.name}" }
            val document = event.document

            val documentParseResult = parserService.parseDocument(event)

            logger.info { "Send Event Start - ${event.document.name}" }

            documentParseResult.forEach { it ->
                val parseEvent = DocumentParse(
                    documentUuid = document.uuid!!,
                    page = it.page,
                    content = it.content,
                    embedContent = it.embedding,
                    chunkNum = it.chunkNum
                )

                eventPublisher.publish(parseEvent)

                logger.info { "Send Event - ${event.document.name} : Page ${it.page} : Chunk ${it.chunkNum}" }
            }

            logger.info { "Send Complete Event - ${event.document.name}" }
            val parseCompleteEvent = DocumentParseComplete(document.uuid!!)
            eventPublisher.publish(parseCompleteEvent)

            logger.info { "Document Parse Success - ${event.document.name}" }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error { "Document Parse fail - ${event.document.name}" }
            eventPublisher.publish(DocumentParseFail(event.document.uuid!!))
        }
    }
}
