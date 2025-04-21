package org.bmserver.docuhelperdocumentparser.core.chunk.event

import org.bmserver.docuhelperdocumentparser.core.domain.event.AbstractEvent
import java.util.UUID

data class DocumentParseComplete(
    val documentUuid: UUID
) : AbstractEvent()