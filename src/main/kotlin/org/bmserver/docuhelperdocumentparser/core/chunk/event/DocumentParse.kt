package org.bmserver.docuhelperdocumentparser.core.chunk.event

import org.bmserver.docuhelperdocumentparser.core.domain.event.AbstractEvent
import java.util.UUID

data class DocumentParse(
    val documentUuid: UUID,
    val page: Int,
    val chunkNum: Int,
    val content: String,
    val embedContent: List<Float>
) : AbstractEvent()