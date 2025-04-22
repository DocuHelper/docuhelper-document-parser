package org.bmserver.docuhelperdocumentparser.ai

import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.stereotype.Component

@Component
class AiService(
    private val ollamaApi: OllamaApi
) {
    fun getEmbeddingValue(text: List<String>): OllamaApi.EmbeddingsResponse {
        val request = OllamaApi.EmbeddingsRequest("bge-m3", text, null, null, null)
        return ollamaApi.embed(request)
    }
}