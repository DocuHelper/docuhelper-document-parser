package org.bmserver.docuhelperdocumentparser.core.model

import org.bmserver.docuhelperdocumentparser.core.domain.BaseDomain
import java.util.UUID

data class Document(
    val name: String,
    val type: DocumentType = DocumentType.SINGLE_COLUMN,
    var state: DocumentState = DocumentState.READING,
    val file: UUID,
    val owner: UUID,
) : BaseDomain() {
}
