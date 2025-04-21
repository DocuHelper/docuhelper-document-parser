package org.bmserver.docuhelperdocumentparser.core.event

import org.bmserver.docuhelperdocumentparser.core.domain.event.AbstractEvent
import org.bmserver.docuhelperdocumentparser.core.model.Document

data class DocumentCreate(val document: Document) : AbstractEvent()
