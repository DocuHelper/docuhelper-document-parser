package org.bmserver.docuhelperdocumentparser.core.chunk.model

import org.bmserver.docuhelperdocumentparser.core.domain.BaseDomain
import java.util.UUID

class Chunk(
    val document: UUID,
    val page: Int,
    val num: Int,
    val content: String,
    val embedContent: List<Float>
) : BaseDomain()