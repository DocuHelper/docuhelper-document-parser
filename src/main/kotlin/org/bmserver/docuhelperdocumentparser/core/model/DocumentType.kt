package org.bmserver.docuhelperdocumentparser.core.model

enum class DocumentType(val group: Any?) {
    HTML(null),
    PPT(null),
    PDF_SINGLE_COLUMN(PDF_Group),
    PDF_MULTI_COLUMN(PDF_Group);

    companion object {
        val PDF = PDF_Group

        object PDF_Group {
            val SINGLE_COLUMN = PDF_SINGLE_COLUMN
            val MULTI_COLUMN = PDF_MULTI_COLUMN
        }
    }
}
