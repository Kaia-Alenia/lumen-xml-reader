package com.lumen.xmlreader.domain.model

data class RecentFile(
    val uriString: String,
    val fileName: String,
    val fileSizeFormatted: String,
    val lastOpenedTimestamp: Long,
    val documentType: XmlDocumentType
)
