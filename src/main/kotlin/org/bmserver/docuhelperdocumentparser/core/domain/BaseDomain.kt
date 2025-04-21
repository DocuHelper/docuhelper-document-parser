package org.bmserver.docuhelperdocumentparser.core.domain

import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.UUID

abstract class BaseDomain(
    @Id var uuid: UUID?,
    var createDt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(uuid = null)
}
