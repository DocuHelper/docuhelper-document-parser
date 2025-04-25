package org.bmserver.docuhelperdocumentparser.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import org.bmserver.docuhelperdocumentparser.ai.AiService
import org.bmserver.docuhelperdocumentparser.core.event.DocumentCreate
import org.bmserver.docuhelperdocumentparser.core.model.DocumentType
import org.bmserver.docuhelperdocumentparser.file.FileService
import org.bmserver.docuhelperdocumentparser.parser.transformer.EmbeddingMetatdataEnricher
import org.bmserver.documentparser.CustomPdfDocumentReader
import org.springframework.ai.chat.transformer.SummaryMetadataEnricher
import org.springframework.ai.document.Document
import org.springframework.ai.document.MetadataMode
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {  }

@Component
class ParserService(
    private val fileService: FileService,
    private val chatModel: OpenAiChatModel,
    private val aiService: AiService
) {

    fun parseDocument(event: DocumentCreate) :List<Document> {
        val document = event.document
        val config = getParserConfig()

        val url = fileService.getFileDownloadURL(document.file).toString()

        val reader = when (document.type) {
            DocumentType.PDF_MULTI_COLUMN -> CustomPdfDocumentReader(url, config)
            DocumentType.PDF_SINGLE_COLUMN -> PagePdfDocumentReader(url, config)
            else -> PagePdfDocumentReader(url, config)
        }

        return  reader.read()
            .let {
                logger.info { "Start Split Document - ${document.name}" }
                splitDocument(it)
            }
            .let {
                logger.info { "Start Split Document - ${document.name}" }
                summaryDocument(it)
            }
            .let {
                logger.info { "Start Embedding Document - ${document.name}" }
                embedDocument(it)
            }

    }

    private fun splitDocument(documents: List<Document>): List<Document> {
        val ts = TokenTextSplitter()
        return ts.split(documents)
    }

    private fun summaryDocument(documents: List<Document>): List<Document> {
        val summaryEnricherTemplate = """
        다음은 해당 섹션의 내용입니다:
        {context_str}

        위 내용을 바탕으로 핵심 주제와 주요 인물, 장소, 기관 등의 개체를 요약해 주세요.

        요약:""".trimIndent()

        val summaryEnricher = SummaryMetadataEnricher(chatModel, null, summaryEnricherTemplate, MetadataMode.ALL)

        return summaryEnricher.transform(documents)
    }

    private fun embedDocument(documents: List<Document>): List<Document> {
        val enricher = EmbeddingMetatdataEnricher(aiService)
        return enricher.transform(documents)
    }

    private fun getParserConfig(): PdfDocumentReaderConfig {
        return PdfDocumentReaderConfig.builder()
            .withPageTopMargin(0) // 페이지 상단 margin 제거
            .withPageExtractedTextFormatter(
                ExtractedTextFormatter.builder()
                    .withNumberOfTopTextLinesToDelete(0) // 텍스트 윗부분 제거 안 함
                    .build()
            )
            .withPagesPerDocument(1) // 페이지당 Document 하나 생성
            .build()
    }
}