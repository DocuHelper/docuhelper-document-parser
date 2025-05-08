package org.bmserver.docuhelperdocumentparser.ai

import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.ai.openai.OpenAiEmbeddingModel
import org.springframework.stereotype.Component

@Component
class AiService(
    private val ollamaApi: OllamaApi,
    private val openAiEmbeddingModel: OpenAiEmbeddingModel,
) {
    fun getEmbeddingValue(text: List<String>): List<FloatArray> {
        val request = OllamaApi.EmbeddingsRequest("bge-m3", text, null, null, null)
        return ollamaApi.embed(request).embeddings
    }

    fun getEmbeddingValueOfOpenAi(text: List<String>): List<FloatArray> {
        return openAiEmbeddingModel.embed(text)

    }
}