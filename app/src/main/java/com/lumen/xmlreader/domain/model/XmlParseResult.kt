package com.lumen.xmlreader.domain.model

sealed interface XmlParseResult {
    data class Success(val documentInfo: XmlDocumentInfo) : XmlParseResult
    data class Error(
        val message: String,
        val line: Int,
        val column: Int,
        val rawSnippet: String? = null
    ) : XmlParseResult
}
