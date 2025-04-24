package org.bmserver.docuhelperdocumentparser.kafka.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.bmserver.docuhelperdocumentparser.ai.AiService
import org.bmserver.docuhelperdocumentparser.core.chunk.event.DocumentParse
import org.bmserver.docuhelperdocumentparser.core.chunk.event.DocumentParseComplete
import org.bmserver.docuhelperdocumentparser.core.chunk.event.DocumentParseFail
import org.bmserver.docuhelperdocumentparser.core.chunk.event.DocumentParseStart
import org.bmserver.docuhelperdocumentparser.core.event.DocumentCreate
import org.bmserver.docuhelperdocumentparser.file.FileService
import org.bmserver.docuhelperdocumentparser.kafka.common.DocuhelperApiEventListener
import org.bmserver.docuhelperdocumentparser.kafka.common.EventPublisher
import org.bmserver.docuhelperdocumentparser.parser.ParserService
import org.springframework.ai.chat.transformer.SummaryMetadataEnricher
import org.springframework.ai.document.MetadataMode
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.stereotype.Component


val logger = KotlinLogging.logger { }

@Component
class DocumentCreateListener(
    private val parserService: ParserService,
    private val fileService: FileService,
    private val aiService: AiService,
    private val chatModel: OpenAiChatModel,
    private val eventPublisher: EventPublisher,
) : DocuhelperApiEventListener<DocumentCreate>() {

    override suspend fun handle(event: DocumentCreate) {
        try {
            logger.info { "Handle DocuhelperApiEventListener - ${event.document.name}" }
            val document = event.document
            eventPublisher.publish(DocumentParseStart(document.uuid!!))

            val url = fileService.getFileDownloadURL(document.file)
            val ts = TokenTextSplitter()
            val summaryEnricherTemplate = """
        다음은 해당 섹션의 내용입니다:
        {context_str}

        위 내용을 바탕으로 핵심 주제와 주요 인물, 장소, 기관 등의 개체를 요약해 주세요.

        요약:""".trimIndent()
            val summaryEnricher = SummaryMetadataEnricher(chatModel, null, summaryEnricherTemplate, MetadataMode.ALL)

            logger.info { "Start Parsing - ${event.document.name}" }

            val documentParseResult = parserService.parseDocument(url)
                .let {
                    println("SPLIT DOCUMENT")
                    ts.split(it)
                }
                .let {
                    println("Summary Document")
                    summaryEnricher.transform(it)
                }

            logger.info { "Embedding Document - ${event.document.name}" }
            val documentEmbedContent = documentParseResult.map { it.text }
                .map { it ?: "" }
                .let { aiService.getEmbeddingValue(it) }

            logger.info { "Send Event Start - ${event.document.name}" }
            var currentPage = 0
            var currentChunk = 0
            documentParseResult.forEachIndexed { index, it ->
                val page = (it.metadata["page_number"]) as Int

                if (currentPage != page) {
                    currentPage = page
                    currentChunk = 1
                }

                val parseEvent = DocumentParse(
                    documentUuid = document.uuid!!,
                    page = page,
                    content = it.text ?: "",
                    embedContent = documentEmbedContent.embeddings[index].toList(),
                    chunkNum = currentChunk
                )

                eventPublisher.publish(parseEvent)

                currentChunk++
                logger.info { "Send Event - ${event.document.name} : Page ${currentPage} : Chunk ${currentChunk}" }
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
