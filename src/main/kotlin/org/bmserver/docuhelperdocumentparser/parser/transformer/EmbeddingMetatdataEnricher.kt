package org.bmserver.docuhelperdocumentparser.parser.transformer

import org.bmserver.docuhelperdocumentparser.ai.AiService
import org.springframework.ai.document.Document
import org.springframework.ai.document.DocumentTransformer

class EmbeddingMetatdataEnricher(
    private val aiService: AiService
) : DocumentTransformer {
    override fun apply(documents: MutableList<Document>): MutableList<Document> {
        val documentEmbededDatas =
            aiService.getEmbeddingValue(documents.map { it.text ?: "" }).embeddings.map { it.toList() }
        return documents.mapIndexed { index, document ->
            document.metadata["embed"] = documentEmbededDatas[index]
            document
        }.toMutableList()
    }
}