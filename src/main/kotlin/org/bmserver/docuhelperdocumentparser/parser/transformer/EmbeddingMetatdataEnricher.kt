package org.bmserver.docuhelperdocumentparser.parser.transformer

import org.bmserver.docuhelperdocumentparser.ai.AiService
import org.springframework.ai.document.Document
import org.springframework.ai.document.DocumentTransformer

class EmbeddingMetatdataEnricher(
    private val aiService: AiService
) : DocumentTransformer {
    override fun apply(documents: MutableList<Document>): MutableList<Document> {
        val documentTexts = documents.map { doc ->
            val exceptKeywords = doc.metadata["except_keywords"]?.toString().orEmpty()
            val text = doc.text.orEmpty()
            "$exceptKeywords\n$text"
        }

        val documentEmbeddedData = aiService
            .getEmbeddingValue(documentTexts)
            .embeddings
            .map { it.toList() }

        return documents.mapIndexed { index, document ->
            document.metadata["embed"] = documentEmbeddedData.getOrNull(index)
            document
        }.toMutableList()
    }
}