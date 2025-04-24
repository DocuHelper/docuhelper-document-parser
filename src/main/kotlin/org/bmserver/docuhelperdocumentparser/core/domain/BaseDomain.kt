package org.bmserver.docuhelperdocumentparser.core.domain

import java.time.LocalDateTime
import java.util.UUID

abstract class BaseDomain(
    var uuid: UUID?,
    var createDt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(uuid = null)
}
