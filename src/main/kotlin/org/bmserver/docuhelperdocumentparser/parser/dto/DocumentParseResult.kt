package org.bmserver.docuhelperdocumentparser.parser.dto

class DocumentParseResult(
    val content: String,
    val embedding: List<Float>,
    val page: Int,
    val chunkNum: Int,
    val keyword: List<String>
) {
}