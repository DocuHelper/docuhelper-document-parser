package org.bmserver.docuhelperdocumentparser.core.chunk.event

import org.bmserver.docuhelperdocumentparser.core.domain.event.AbstractEvent
import java.util.UUID

data class DocumentParseStart(
    val documentUuid: UUID
) : AbstractEvent()