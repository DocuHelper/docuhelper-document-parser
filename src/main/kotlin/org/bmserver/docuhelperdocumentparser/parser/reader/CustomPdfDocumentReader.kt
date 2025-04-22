package org.bmserver.documentparser

import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.document.DocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.reader.pdf.layout.PDFLayoutTextStripperByArea
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.util.StringUtils
import java.awt.geom.Rectangle2D
import java.io.IOException
import java.util.LinkedList

/**
 * 2열(Left/Right) 구조의 PDF 페이지를 읽어들여
 * 좌우 컬럼 순서대로 텍스트를 추출하는 커스텀 리더입니다.
 */
class CustomPdfDocumentReader : DocumentReader {

    companion object {
        const val METADATA_START_PAGE_NUMBER = "page_number"
        const val METADATA_END_PAGE_NUMBER = "end_page_number"
        const val METADATA_FILE_NAME = "file_name"
        private const val DEFAULT_ESTIMATED_LINE_HEIGHT = 14f
        private const val DEFAULT_MIN_CHARS_PER_COLUMN = 5
    }

    private val document: PDDocument
    private val resourceFileName: String?
    private val config: PdfDocumentReaderConfig
    private val logger = LoggerFactory.getLogger(CustomPdfDocumentReader::class.java)

    constructor(resourceUrl: String) : this(DefaultResourceLoader().getResource(resourceUrl))
    constructor(pdfResource: Resource) : this(pdfResource, PdfDocumentReaderConfig.defaultConfig())
    constructor(resourceUrl: String, config: PdfDocumentReaderConfig)
            : this(DefaultResourceLoader().getResource(resourceUrl), config)

    constructor(pdfResource: Resource, config: PdfDocumentReaderConfig) {
        try {
            val parser = PDFParser(org.apache.pdfbox.io.RandomAccessReadBuffer(pdfResource.inputStream))

            this.document = parser.parse()
            this.resourceFileName = pdfResource.filename
            this.config = config
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun get(): List<Document> {
        val readDocuments = mutableListOf<Document>()
        try {
            val pdfTextStripper = PDFLayoutTextStripperByArea()
            var pageNumber = 0
            var pagesPerDocument = 0
            var startPageNumber = 0
            val pageTextGroupList = mutableListOf<String>()

            val pages = document.documentCatalog.pages
            val totalPages = pages.count
            val logFrequency = if (totalPages > 10) totalPages / 10 else 1
            var counter = 0
            var lastPage: PDPage? = null

            for (page in pages) {
                lastPage = page
                if (counter % logFrequency == 0 && counter / logFrequency < 10) {
                    logger.info("Processing PDF page: ${counter + 1}")
                }
                counter++
                pagesPerDocument++

                // 페이징 단위 맞춤
                if (config.pagesPerDocument != PdfDocumentReaderConfig.ALL_PAGES
                    && pagesPerDocument >= config.pagesPerDocument
                ) {
                    pagesPerDocument = 0
                    val aggregated = pageTextGroupList.joinToString("")
                    if (StringUtils.hasText(aggregated)) {
                        readDocuments.add(toDocument(page, aggregated, startPageNumber, pageNumber))
                    }
                    pageTextGroupList.clear()
                    startPageNumber = pageNumber + 1
                }

                // 동적 레이아웃 감지: 글자 위치 획득
                val positionStripper = PositionCapturingStripper(document)
                val allArea = positionStripper.capture(pageNumber + 1, 0, 0)

                val test = allArea
                    .groupBy { it.y } // 글자 -> 한줄
                    .values
                    //TODO 이거 왜 .. 내맘대로.. 안될까...
                    .groupBy { Math.ceil((it.first().y.toInt() / 10).toDouble()) * 10 } // 1단 2단
                    .values

                val test_area = test.map {
                    it.map { rects ->
                        val minX = rects.minOf { it.x }
                        val minY = rects.minOf { it.y }
                        val maxX = rects.maxOf { it.x + it.width }
                        val maxY = rects.maxOf { it.y + it.height }
                        Rectangle2D.Float(
                            minX.toFloat(),
                            minY.toFloat(),
                            (maxX - minX).toFloat(),
                            (maxY - minY).toFloat()
                        )
                    }
                }

                val mergedAreas = test_area.map { rects ->
                    mergeRectangles(rects, threshold = 5f) // TODO 글자크기에 따라 달라지게 해야할듯
                }

                val test_area_string = mergedAreas.map {
                    it.mapIndexed { idx, rect ->
                        pdfTextStripper.addRegion("region$idx", rect)
                        pdfTextStripper.extractRegions(page)
                        pdfTextStripper.removeRegion("region$idx")
                        pdfTextStripper.getTextForRegion("region$idx")
                            .trim()
                            .replace("\u0000", "")
                    }
                        .let { LinkedList(it) }
                }

                val content = mutableListOf<String>()
                while (test_area_string.filter { it.isNotEmpty() }.isNotEmpty()) {
                    test_area_string.forEach {
                        if (it.peekFirst() != null) {
                            content += it.pollFirst() + "\n"
                        }
                    }
                }

                pageTextGroupList += content
                pageNumber++

            }

            return readDocuments
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun mergeRectangles(
        rects: List<Rectangle2D.Float>,
        threshold: Float
    ): List<Rectangle2D.Float> {
        if (rects.isEmpty()) return emptyList()
        val merged = mutableListOf<Rectangle2D.Float>()
        var current = rects[0]
        for (i in 1 until rects.size) {
            val next = rects[i]
            val gap = next.x - (current.x + current.width)
            if (gap <= threshold) {
                val newMinX = minOf(current.x, next.x)
                val newMinY = minOf(current.y, next.y)
                val newMaxX = maxOf(current.x + current.width, next.x + next.width)
                val newMaxY = maxOf(current.y + current.height, next.y + next.height)
                current = Rectangle2D.Float(
                    newMinX.toFloat(),
                    newMinY.toFloat(),
                    (newMaxX - newMinX).toFloat(),
                    (newMaxY - newMinY).toFloat()
                )
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)
        return merged
    }


    private fun toDocument(
        page: PDPage,
        docText: String,
        startPageNumber: Int,
        endPageNumber: Int
    ): Document {
        val doc = Document(docText)
        doc.metadata[METADATA_START_PAGE_NUMBER] = startPageNumber
        if (startPageNumber != endPageNumber) {
            doc.metadata[METADATA_END_PAGE_NUMBER] = endPageNumber
        }
        resourceFileName?.let { doc.metadata[METADATA_FILE_NAME] = it }
        return doc
    }
}
