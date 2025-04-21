package org.bmserver.docuhelperdocumentparser.parser

import org.bmserver.documentparser.CustomPdfDocumentReader
import org.springframework.ai.document.Document
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.stereotype.Component
import java.net.URL

@Component
class ParserService {

    fun parseDocument(url: URL):List<Document> {
        val config =
            PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0) // 페이지 상단 margin 제거
                .withPageExtractedTextFormatter(
                    ExtractedTextFormatter.builder()
                        .withNumberOfTopTextLinesToDelete(0) // 텍스트 윗부분 제거 안 함
                        .build()
                )
                .withPagesPerDocument(1) // 페이지당 Document 하나 생성
                .build()

        val reader = CustomPdfDocumentReader(url.toString(), config)

        val documents = reader.read() // 문단 단위로 Document 리스트 반환

        return documents
    }

}